package com.example.jearmillos2.demoimageviewflip;

import android.os.Environment;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;



/**
 * Created by jhuerfano on 08/04/2015.
 */
public class FileDownloader {
    private static final int MEGABYTE = 1024 * 1024;
    private Cipher cipher;
    private Cipher cipher1;
    private KeyPairGenerator kpg;
    private KeyPair kp;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private File directorio;

    public void downloadFile(String fileUrl, File directory, String rString, File directoryFolder, String cifrado) {
        try {
            directorio = directoryFolder;
            URL url = new URL(fileUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //urlConnection.setRequestMethod("GET");
            //urlConnection.setDoOutput(true);
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            if (cifrado.equals("AES")) {
                aesCypher(rString, inputStream, directory);
            } else {
                rsaCypher(rString, inputStream, directory);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }



    public void aesCypher(String rString, InputStream inputStream, File directory) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IOException {
        // Length is 16 byte
        // Careful when taking user input!!! http://stackoverflow.com/a/3452620/1188357

        SecretKeySpec sks = new SecretKeySpec(rString.getBytes(), "AES");
        // Create cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, sks);
        FileOutputStream fileOutputStream = new FileOutputStream(directory);
        CipherOutputStream cos = new CipherOutputStream(fileOutputStream, cipher);
        // Write
        byte[] buffer = new byte[MEGABYTE];
        int bufferLength = 0;
        while ((bufferLength = inputStream.read(buffer)) > 0) {
            cos.write(buffer, 0, bufferLength);
        }
        cos.flush();
        cos.close();
        fileOutputStream.close();
        System.out.println("AES encryp complete");
    }

    public void rsaCypher(String rString, InputStream inputStream, File directory) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException {
        kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        kp = kpg.genKeyPair();
        privateKey = kp.getPrivate();
        cipher = Cipher.getInstance("RSA");
        File root = new File(Environment.getExternalStorageDirectory() + "/Notes/", "rsapublick.txt");
        //Read text from file
        if (root.exists()) {
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(root));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
                byte[] keyBytes = Base64.decode(text.toString().getBytes("utf-8"), 0);
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                publicKey = keyFactory.generatePublic(spec);
            } catch (IOException e) {
                //You'll need to add proper error handling here
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        } else {
            publicKey = kp.getPublic();
            byte[] pKbytes = Base64.encode(publicKey.getEncoded(), 0);
            String pK = new String(pKbytes);
            String pubKey = "-----BEGIN public KEY-----\n" + pK + "-----END public KEY-----\n";
            System.out.println(pubKey);
            generateNoteOnSD("rsapublick.txt", pK, directorio);
        }
        this.cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] bytes = getBytesFromInputStream(inputStream);
        byte[] encrypted = blockCipher(bytes, Cipher.ENCRYPT_MODE);
        FileOutputStream fileOutputStream = new FileOutputStream(directory);
        fileOutputStream.write(encrypted);
        fileOutputStream.close();
        System.out.println("Encryptado RSA Finalizado");
        root = new File(Environment.getExternalStorageDirectory() + "/Notes/", "rsaOrivatek.txt");
        if (!root.exists()) {
            byte[] pKbytes = Base64.encode(getPrivateKey().getEncoded(), 0);
            String pK = new String(pKbytes);
            String pubKey = "-----BEGIN private KEY-----\n" + pK + "-----END private KEY-----\n";
            System.out.println(pubKey);
            generateNoteOnSD("rsaOrivatek.txt", pK, directorio);
        }

    }

    public void rsaDecrypt(PrivateKey privateKey, FileInputStream fileInputStream, FileOutputStream fileOutputStream) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, NoSuchPaddingException, NoSuchAlgorithmException {
        cipher = Cipher.getInstance("RSA");
        this.cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = blockCipher(getBytesFromInputStream(fileInputStream), Cipher.DECRYPT_MODE);
        fileOutputStream.write(decrypted);
        fileOutputStream.close();
    }

    private byte[] blockCipher(byte[] bytes, int mode) throws IllegalBlockSizeException, BadPaddingException {
        // string initialize 2 buffers.
        // scrambled will hold intermediate results
        byte[] scrambled = new byte[0];

        // toReturn will hold the total result
        byte[] toReturn = new byte[0];
        // if we encrypt we use 100 byte long blocks. Decryption requires 128 byte long blocks (because of RSA)
        int length = (mode == Cipher.ENCRYPT_MODE) ? 100 : 128;

        // another buffer. this one will hold the bytes that have to be modified in this step
        byte[] buffer = new byte[length];

        for (int i = 0; i < bytes.length; i++) {

            // if we filled our buffer array we have our block ready for de- or encryption
            if ((i > 0) && (i % length == 0)) {
                //execute the operation
                scrambled = cipher.doFinal(buffer);
                // add the result to our total result.
                toReturn = append(toReturn, scrambled);
                // here we calculate the length of the next buffer required
                int newlength = length;

                // if newlength would be longer than remaining bytes in the bytes array we shorten it.
                if (i + length > bytes.length) {
                    newlength = bytes.length - i;
                }
                // clean the buffer array
                buffer = new byte[newlength];
            }
            // copy byte into our buffer.
            buffer[i % length] = bytes[i];
        }

        // this step is needed if we had a trailing buffer. should only happen when encrypting.
        // example: we encrypt 110 bytes. 100 bytes per run means we "forgot" the last 10 bytes. they are in the buffer array
        scrambled = cipher.doFinal(buffer);

        // final step before we can return the modified data.
        toReturn = append(toReturn, scrambled);

        return toReturn;
    }

    private static byte[] append(byte[] prefix, byte[] suffix) {
        byte[] toReturn = new byte[prefix.length + suffix.length];
        for (int i = 0; i < prefix.length; i++) {
            toReturn[i] = prefix[i];
        }
        for (int i = 0; i < suffix.length; i++) {
            toReturn[i + prefix.length] = suffix[i];
        }
        return toReturn;
    }

    public byte[] getBytesFromInputStream(InputStream is) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[0xFFFF];

            for (int len; (len = is.read(buffer)) != -1; )
                os.write(buffer, 0, len);

            os.flush();

            return os.toByteArray();
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void generateNoteOnSD(String sFileName, String sBody, File directorio) {
        try {
            File root = new File(directorio, "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

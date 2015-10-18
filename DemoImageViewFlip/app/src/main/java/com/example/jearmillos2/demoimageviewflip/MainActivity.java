package com.example.jearmillos2.demoimageviewflip;

import se.emilsjolander.flipview.FlipView;
import se.emilsjolander.flipview.FlipView.OnFlipListener;
import se.emilsjolander.flipview.FlipView.OnOverFlipListener;
import se.emilsjolander.flipview.OverFlipMode;

import android.app.Activity;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.jearmillos2.demoimageviewflip.common.activities.SampleActivityBase;

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
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends SampleActivityBase implements FlipAdapter.Callback, OnFlipListener, OnOverFlipListener {
    private SecureRandom random = new SecureRandom();
    public static final String TAG = "MainActivity";

    public static final String FRAGTAG = "BeamLargeFilesFragment";
    public static final String ENCRYPTION = "RSA";
    private FlipView mFlipView;
    private FlipAdapter mAdapter;
    //File Descriptor for rendered Pdf file
    private ParcelFileDescriptor mFileDescriptor;
    //For rendering a PDF document
    private PdfRenderer mPdfRenderer;
    public static SessionIdentifierGenerator si = new SessionIdentifierGenerator();
    private File pdfFile;
    private String kString = "MyDifficultPassw";
    byte[] key;

    FileDownloader fileDownloader = new FileDownloader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*BeamLargeFilesFragment fragment = new BeamLargeFilesFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(fragment, FRAGTAG);
        transaction.commit();*/

        mFlipView = (FlipView) findViewById(R.id.flip_view);
        System.out.println(getExternalFilesDir(null));
        pdfFile = new File(Environment.getExternalStorageDirectory(), "sbfile456.pdf");
        if (pdfFile.exists() && !pdfFile.isDirectory()) {
            try {
                openRenderer(Environment.getExternalStorageDirectory() + "/sbfile456.pdf", kString);
                mAdapter = new FlipAdapter(this, mPdfRenderer);
                mAdapter.setCallback(this);
                mFlipView.setAdapter(mAdapter);
                mFlipView.setOnFlipListener(this);
                mFlipView.peakNext(false);
                mFlipView.setOverFlipMode(OverFlipMode.RUBBER_BAND);
                mFlipView.setEmptyView(findViewById(R.id.empty_view));
                mFlipView.setOnOverFlipListener(this);

            } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
        } else {
            new DownloadFile().execute("http://www.pdf995.com/samples/pdf.pdf", kString);

        }
        //new DownloadFile().execute("http://192.168.0.85:7001/PDFjs/demoresponse?p_data={id:105,tab:ANL_EVENTOS,cid:EVENTO_ID,cnom:NOMBRE,c:INFORME,mime:FORMATO,ds:SpervyDS}",kString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*switch (item.getItemId()) {
		case R.id.prepend:
			mAdapter.addItemsBefore(5);
			return true;
		}
		return super.onOptionsItemSelected(item);*/
        return true;
    }

    @Override
    public void onPageRequested(int page) {
        mFlipView.smoothFlipTo(page);
    }

    @Override
    public void onFlippedToPage(FlipView v, int position, long id) {
        Log.i("pageflip", "Page: " + position);
		/*if(position > mFlipView.getPageCount()-3 && mFlipView.getPageCount()<30){
			mAdapter.addItems(5);
		}*/
    }

    @Override
    public void onOverFlip(FlipView v, OverFlipMode mode,
                           boolean overFlippingPrevious, float overFlipDistance,
                           float flipDistancePerPage) {
        Log.i("overflip", "overFlipDistance = " + overFlipDistance);
    }

    /**
     * API for initializing file descriptor and pdf renderer after selecting pdf from list
     *
     * @param filePath
     */
    private void openRenderer(String filePath, String kString) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException {
        FileInputStream fis = new FileInputStream(filePath);
        FileOutputStream fos = new FileOutputStream(getBaseContext().getCacheDir() + "/sbfile456b.pdf");

        if (ENCRYPTION.equals("AES")) {
            SecretKeySpec sks = new SecretKeySpec(kString.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, sks);
            CipherInputStream cis = new CipherInputStream(fis, cipher);
            int b;
            byte[] d = new byte[8];
            while ((b = cis.read(d)) != -1) {
                fos.write(d, 0, b);
            }
            fos.flush();
            fos.close();
            cis.close();
        } else {
            File root = new File(Environment.getExternalStorageDirectory() + "/Notes/", "rsaOrivatek.txt");
            //Read text from file
            StringBuilder text = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(root));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            } catch (IOException e) {
                //You'll need to add proper error handling here
            }
            System.out.println(text.toString());

            byte[] keyBytes = Base64.decode(text.toString().getBytes("utf-8"), 0);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PrivateKey priv = fact.generatePrivate(keySpec);
            fileDownloader.rsaDecrypt(priv,fis,fos);
            System.out.println("RSA decrypt success");
        }
        File file = new File(getBaseContext().getCacheDir() + "/sbfile456b.pdf");
        try {
            mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            //mFileDescriptor = getBaseContext().getAssets().openFd("sample.pdf").getParcelFileDescriptor();
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
            file.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class DownloadFile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String fileUrl = strings[0];
            pdfFile = new File(Environment.getExternalStorageDirectory(), "sbfile456.pdf");
            try {
                if (!pdfFile.exists() && !pdfFile.isDirectory()) {
                    boolean crear = pdfFile.createNewFile();
                    Log.i("out", "Creaciòn de archivo : " + crear);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileDownloader.downloadFile(fileUrl, pdfFile, strings[1], Environment.getExternalStorageDirectory(), ENCRYPTION);

            return null;
        }

    }

    public void generateNoteOnSD(String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

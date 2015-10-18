package com.example.jearmillos2.demoimageviewflip;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by jhuerfano on 21/04/2015.
 */
public class SessionIdentifierGenerator {
    private SecureRandom random = new SecureRandom();
    public String number;

    public SessionIdentifierGenerator() {
        number = nextSessionId();
    }

    public String nextSessionId() {
        return new BigInteger(128, random).toString(32);
    }

}

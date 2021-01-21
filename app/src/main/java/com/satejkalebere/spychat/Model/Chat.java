package com.satejkalebere.spychat.Model;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Chat {

    private String sender;
    private String receiver;
    private String message;
    private String msg;
    private String msg_time;
    private boolean isseen;
    private byte encryptionkey[] = {8, 111, 54, 86, 105, 4, -37, -25, -65, 87, 18, 26, 5, -105, 114, -52};
    private Cipher cipher,decipher;
    private SecretKeySpec secretKeySpec;


    public Chat(String sender, String receiver, String message, boolean isseen, String msg_time) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.msg_time = msg_time;

    }

    public Chat() {
    }

    public String getMsg_time() {
        return msg_time;
    }

    public void setMsg_time(String msg_time) {
        this.msg_time = msg_time;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {

        try {
            cipher= Cipher.getInstance("AES");
            decipher=Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec=new SecretKeySpec(encryptionkey,"AES");
        try {
            msg=decryptionMethod(message);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return msg;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public boolean isIsseen() {
        return isseen;
    }

    private String decryptionMethod(String string) throws UnsupportedEncodingException {

        byte[] EncryptedByte=string.getBytes("ISO-8859-1");
        String decryptedString=string;
        byte[] decryption;
        try {
            decipher.init(cipher.DECRYPT_MODE,secretKeySpec);
            decryption=decipher.doFinal(EncryptedByte);
            decryptedString= new String(decryption);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptedString;

    }
}

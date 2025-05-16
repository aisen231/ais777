package com.example.ais777.util;

import android.util.Base64;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {
    private static final String AES = "AES";
    private static final String AES_CIPHER = "AES/CBC/PKCS5Padding";
    private static final String PBKDF2 = "PBKDF2WithHmacSHA256";
    private static final int KEY_LEN = 256;           // длина ключа в битах
    private static final int ITERATIONS = 10000;      // количество итераций PBKDF2
    private static final SecureRandom random = new SecureRandom();

    // 1) Генерирует случайную соль (16 байт)
    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    // 2) По паролю и соли генерирует SecretKey для AES
    public static SecretKey deriveKey(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LEN);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, AES);
    }

    // 3) Шифрование: возвращает строку Base64(IV) + ":" + Base64(шифртекст)
    public static String encrypt(SecretKey key, String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CIPHER);
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] cipherBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
        String ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP);
        String ctB64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP);
        return ivB64 + ":" + ctB64;
    }

    // 4) Дешифрование: принимает строку IV:шифртекст в Base64, возвращает исходный текст
    public static String decrypt(SecretKey key, String encrypted) throws Exception {
        String[] parts = encrypted.split(":");
        byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
        byte[] ct = Base64.decode(parts[1], Base64.NO_WRAP);

        Cipher cipher = Cipher.getInstance(AES_CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] plainBytes = cipher.doFinal(ct);
        return new String(plainBytes, "UTF-8");
    }
}
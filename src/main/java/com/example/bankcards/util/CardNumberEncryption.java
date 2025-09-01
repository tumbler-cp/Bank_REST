package com.example.bankcards.util;

import java.util.Base64;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CardNumberEncryption {

    @Value("${encryption.algorithm}")
    private String ALGHTORITHM;

    private String TRANSFORMATION = "AES";

    @Value("${encryption.secret}")
    private String SECRET;

    private final Pattern CARD_PATTERN = Pattern.compile("^\\d{16}$");

    public String encryptCardNumber(String cardNumber) {
        validateCardNumbber(cardNumber);

        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET.getBytes(), ALGHTORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Card encryption error");
        }
    }

    public String decryptCardNumber(String cardNumber) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET.getBytes(), ALGHTORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decodedBytes = Base64.getDecoder().decode(cardNumber);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            String decryptedNumber = new String(decryptedBytes);
            validateCardNumbber(decryptedNumber);

            return decryptedNumber;
        } catch (Exception e) {
            throw new RuntimeException("Card decryption error");
        }
    }

    public String maskCardNumber(String cardNumber) {
        validateCardNumbber(cardNumber);

        return "**** **** **** " + cardNumber.substring(12);
    }

    private void validateCardNumbber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new RuntimeException("Blank card number is not allowed");
        }

        String cleanNumber = cardNumber.replaceAll("\\s", "");

        if (!CARD_PATTERN.matcher(cleanNumber).matches()) {
            throw new RuntimeException("Invalid card number format");
        }
    }
}

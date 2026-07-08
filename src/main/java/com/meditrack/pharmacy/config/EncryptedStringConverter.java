package com.meditrack.pharmacy.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * JPA AttributeConverter that encrypts/decrypts String fields using AES/CBC/PKCS5Padding.
 * A random 16-byte IV is generated per encryption call, so encrypting the same value
 * twice produces different ciphertext. The IV is prepended to the ciphertext and the
 * result is Base64-encoded for storage.
 */
@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptedStringConverter(
            @Value("${app.encryption.secret:MediTrack-Default-Secret-Change-In-Prod}") String secret) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(secret.getBytes(StandardCharsets.UTF_8));
            // AES-256 uses 32 bytes; AES-128 uses 16. Use first 16 for broader compatibility.
            this.secretKey = new SecretKeySpec(Arrays.copyOf(keyBytes, 16), KEY_ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encryption key", e);
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isBlank()) {
            return attribute;
        }
        try {
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to ciphertext
            byte[] ivAndCiphertext = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, ivAndCiphertext, 0, iv.length);
            System.arraycopy(encrypted, 0, ivAndCiphertext, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(ivAndCiphertext);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }
        try {
            byte[] ivAndCiphertext = Base64.getDecoder().decode(dbData);
            byte[] iv = Arrays.copyOfRange(ivAndCiphertext, 0, 16);
            byte[] ciphertext = Arrays.copyOfRange(ivAndCiphertext, 16, ivAndCiphertext.length);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
}

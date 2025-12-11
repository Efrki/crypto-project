package com.example.crypto_project.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TwofishTest {

    private Twofish twofish;

    @BeforeEach
    void setUp() {
        twofish = new Twofish();
    }

    @Test
    @DisplayName("Test Twofish basic encryption and decryption")
    void testBasicEncryptionDecryption() {
        byte[] key = hexStringToByteArray("000102030405060708090A0B0C0D0E0F");
        byte[] plaintext = hexStringToByteArray("00112233445566778899AABBCCDDEEFF");
        
        byte[] encrypted = twofish.encrypt(plaintext, key);
        byte[] decrypted = twofish.decrypt(encrypted, key);
        
        assertArrayEquals(plaintext, decrypted, "Decryption should restore original plaintext");
        assertFalse(java.util.Arrays.equals(plaintext, encrypted), "Encrypted data should differ from plaintext");
    }

    @Test
    @DisplayName("Test Twofish with different key sizes")
    void testDifferentKeySizes() {
        byte[] plaintext = hexStringToByteArray("00112233445566778899AABBCCDDEEFF");
        
        // 128-bit key
        byte[] key128 = hexStringToByteArray("000102030405060708090A0B0C0D0E0F");
        byte[] encrypted128 = twofish.encrypt(plaintext, key128);
        byte[] decrypted128 = twofish.decrypt(encrypted128, key128);
        assertArrayEquals(plaintext, decrypted128);
        
        // 192-bit key
        byte[] key192 = hexStringToByteArray("000102030405060708090A0B0C0D0E0F1011121314151617");
        byte[] encrypted192 = twofish.encrypt(plaintext, key192);
        byte[] decrypted192 = twofish.decrypt(encrypted192, key192);
        assertArrayEquals(plaintext, decrypted192);
        
        // 256-bit key
        byte[] key256 = hexStringToByteArray("000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F");
        byte[] encrypted256 = twofish.encrypt(plaintext, key256);
        byte[] decrypted256 = twofish.decrypt(encrypted256, key256);
        assertArrayEquals(plaintext, decrypted256);
    }

    @Test
    @DisplayName("Test block size")
    void testBlockSize() {
        assertEquals(16, twofish.getBlockSize(), "Twofish block size should be 16 bytes");
    }

    @Test
    @DisplayName("Should throw exception for invalid block size")
    void testInvalidBlockSize() {
        byte[] key = new byte[16];
        byte[] invalidData = new byte[10];
        
        assertThrows(IllegalArgumentException.class, () -> twofish.encrypt(invalidData, key));
        assertThrows(IllegalArgumentException.class, () -> twofish.decrypt(invalidData, key));
    }

    @Test
    @DisplayName("Test with all zeros")
    void testAllZeros() {
        byte[] key = new byte[16];
        byte[] plaintext = new byte[16];
        
        byte[] encrypted = twofish.encrypt(plaintext, key);
        byte[] decrypted = twofish.decrypt(encrypted, key);
        
        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("Test with all ones")
    void testAllOnes() {
        byte[] key = new byte[16];
        java.util.Arrays.fill(key, (byte) 0xFF);
        byte[] plaintext = new byte[16];
        java.util.Arrays.fill(plaintext, (byte) 0xFF);
        
        byte[] encrypted = twofish.encrypt(plaintext, key);
        byte[] decrypted = twofish.decrypt(encrypted, key);
        
        assertArrayEquals(plaintext, decrypted);
    }

    // Вспомогательный метод для преобразования HEX-строки в массив байт
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
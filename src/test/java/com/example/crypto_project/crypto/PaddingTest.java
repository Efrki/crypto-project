package com.example.crypto_project.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PaddingTest {

    @Test
    void testPaddingModes() {
        RC6 cipher = new RC6();
        BlockCipherModes modes = new BlockCipherModes(cipher);
        
        byte[] key = new byte[16];
        byte[] iv = modes.generateIV();
        byte[] data = "Hello World!".getBytes();
        
        // Тест PKCS7
        byte[] encrypted = modes.encrypt(data, key, iv, CipherMode.CBC, PaddingMode.PKCS7);
        byte[] decrypted = modes.decrypt(encrypted, key, iv, CipherMode.CBC, PaddingMode.PKCS7);
        assertArrayEquals(data, decrypted);
        
        // Тест ZEROS
        encrypted = modes.encrypt(data, key, iv, CipherMode.CBC, PaddingMode.ZEROS);
        decrypted = modes.decrypt(encrypted, key, iv, CipherMode.CBC, PaddingMode.ZEROS);
        assertArrayEquals(data, decrypted);
        
        // Тест ANSI_X923
        encrypted = modes.encrypt(data, key, iv, CipherMode.CBC, PaddingMode.ANSI_X923);
        decrypted = modes.decrypt(encrypted, key, iv, CipherMode.CBC, PaddingMode.ANSI_X923);
        assertArrayEquals(data, decrypted);
        
        // Тест ISO_10126
        encrypted = modes.encrypt(data, key, iv, CipherMode.CBC, PaddingMode.ISO_10126);
        decrypted = modes.decrypt(encrypted, key, iv, CipherMode.CBC, PaddingMode.ISO_10126);
        assertArrayEquals(data, decrypted);
    }
}
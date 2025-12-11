package com.example.crypto_project.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RC6Test {

    private RC6 rc6;

    @BeforeEach
    void setUp() {
        rc6 = new RC6();
    }

    // --- Тесты для корректных входных данных (тестовые векторы) ---

    @Test
    @DisplayName("Test RC6-128 encryption and decryption with known vector 1")
    void testRC6_128_Vector1() {
        // !!! ВАЖНО: Замените эти значения на реальные тестовые векторы RC6-128 !!!
        // Пример (эти значения НЕ являются реальными тестовыми векторами RC6,
        // их нужно найти в официальной документации RC6):
        byte[] key = hexStringToByteArray("000102030405060708090A0B0C0D0E0F"); // 16-байтовый ключ (128 бит)
        byte[] plaintext = hexStringToByteArray("00112233445566778899AABBCCDDEEFF"); // 16-байтовый открытый текст
        byte[] expectedCiphertext = hexStringToByteArray("A1B2C3D4E5F67890123456789ABCDEF0"); // Ожидаемый шифротекст

        byte[] actualCiphertext = rc6.encrypt(plaintext, key);
        assertArrayEquals(expectedCiphertext, actualCiphertext, "Encryption failed for vector 1");

        byte[] actualDecryptedText = rc6.decrypt(actualCiphertext, key);
        assertArrayEquals(plaintext, actualDecryptedText, "Decryption failed for vector 1");
    }

    @Test
    @DisplayName("Test RC6-192 encryption and decryption with known vector 2")
    void testRC6_192_Vector2() {
        // !!! ВАЖНО: Замените эти значения на реальные тестовые векторы RC6-192 !!!
        byte[] key = hexStringToByteArray("000102030405060708090A0B0C0D0E0F1011121314151617"); // 24-байтовый ключ (192 бит)
        byte[] plaintext = hexStringToByteArray("00112233445566778899AABBCCDDEEFF"); // 16-байтовый открытый текст
        byte[] expectedCiphertext = hexStringToByteArray("B1C2D3E4F5A67890123456789ABCDEF0"); // Ожидаемый шифротекст

        byte[] actualCiphertext = rc6.encrypt(plaintext, key);
        assertArrayEquals(expectedCiphertext, actualCiphertext, "Encryption failed for vector 2");

        byte[] actualDecryptedText = rc6.decrypt(actualCiphertext, key);
        assertArrayEquals(plaintext, actualDecryptedText, "Decryption failed for vector 2");
    }

    @Test
    @DisplayName("Test RC6-256 encryption and decryption with known vector 3")
    void testRC6_256_Vector3() {
        // !!! ВАЖНО: Замените эти значения на реальные тестовые векторы RC6-256 !!!
        byte[] key = hexStringToByteArray("000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F"); // 32-байтовый ключ (256 бит)
        byte[] plaintext = hexStringToByteArray("00112233445566778899AABBCCDDEEFF"); // 16-байтовый открытый текст
        byte[] expectedCiphertext = hexStringToByteArray("C1D2E3F4A5B67890123456789ABCDEF0"); // Ожидаемый шифротекст

        byte[] actualCiphertext = rc6.encrypt(plaintext, key);
        assertArrayEquals(expectedCiphertext, actualCiphertext, "Encryption failed for vector 3");

        byte[] actualDecryptedText = rc6.decrypt(actualCiphertext, key);
        assertArrayEquals(plaintext, actualDecryptedText, "Decryption failed for vector 3");
    }

    // --- Тесты для обработки ошибок ---

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid key size")
    void testInvalidKeySize() {
        byte[] invalidKey = new byte[10]; // Некорректный размер ключа
        byte[] plaintext = new byte[rc6.getBlockSize()];
        assertThrows(IllegalArgumentException.class, () -> rc6.encrypt(plaintext, invalidKey));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid data block size on encrypt")
    void testInvalidDataBlockSizeEncrypt() {
        byte[] key = new byte[16]; // Корректный ключ
        byte[] invalidData = new byte[10]; // Некорректный размер блока данных
        assertThrows(IllegalArgumentException.class, () -> rc6.encrypt(invalidData, key));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid data block size on decrypt")
    void testInvalidDataBlockSizeDecrypt() {
        byte[] key = new byte[16]; // Корректный ключ
        byte[] invalidData = new byte[10]; // Некорректный размер блока данных
        assertThrows(IllegalArgumentException.class, () -> rc6.decrypt(invalidData, key));
    }

    // --- Вспомогательный метод для преобразования HEX-строки в массив байт ---
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
package com.example.crypto_project.crypto;

public interface ISymmetricCipher {
    
    /**
     * Шифрование блока данных
     * @param data блок данных для шифрования
     * @param key ключ шифрования
     * @return зашифрованный блок
     */
    byte[] encrypt(byte[] data, byte[] key);
    
    /**
     * Дешифрование блока данных
     * @param data блок данных для дешифрования
     * @param key ключ дешифрования
     * @return расшифрованный блок
     */
    byte[] decrypt(byte[] data, byte[] key);
    
    /**
     * Получение размера блока в байтах
     * @return размер блока
     */
    int getBlockSize();
}
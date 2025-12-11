package com.example.crypto_project.crypto;

import java.security.SecureRandom;
import java.util.Arrays;

public class BlockCipherModes {
    
    private final ISymmetricCipher cipher;
    private final SecureRandom random = new SecureRandom();
    
    public BlockCipherModes(ISymmetricCipher cipher) {
        this.cipher = cipher;
    }
    
    // Генерация IV
    public byte[] generateIV() {
        byte[] iv = new byte[cipher.getBlockSize()];
        random.nextBytes(iv);
        return iv;
    }
    
    // Основной метод шифрования
    public byte[] encrypt(byte[] data, byte[] key, byte[] iv, CipherMode mode, PaddingMode paddingMode) {
        switch (mode) {
            case ECB: return encryptECB(data, key, paddingMode);
            case CBC: return encryptCBC(data, key, iv, paddingMode);
            case PCBC: return encryptPCBC(data, key, iv, paddingMode);
            case CFB: return encryptCFB(data, key, iv);
            case OFB: return encryptOFB(data, key, iv);
            case CTR: return encryptCTR(data, key, iv);
            case RANDOM_DELTA: return encryptRandomDelta(data, key, iv);
            default: throw new IllegalArgumentException("Unsupported mode: " + mode);
        }
    }
    
    // Основной метод дешифрования
    public byte[] decrypt(byte[] data, byte[] key, byte[] iv, CipherMode mode, PaddingMode paddingMode) {
        switch (mode) {
            case ECB: return decryptECB(data, key, paddingMode);
            case CBC: return decryptCBC(data, key, iv, paddingMode);
            case PCBC: return decryptPCBC(data, key, iv, paddingMode);
            case CFB: return decryptCFB(data, key, iv);
            case OFB: return decryptOFB(data, key, iv);
            case CTR: return decryptCTR(data, key, iv);
            case RANDOM_DELTA: return decryptRandomDelta(data, key, iv);
            default: throw new IllegalArgumentException("Unsupported mode: " + mode);
        }
    }
    
    // ECB - каждый блок шифруется независимо
    private byte[] encryptECB(byte[] data, byte[] key, PaddingMode paddingMode) {
        int blockSize = cipher.getBlockSize();
        byte[] padded = addPadding(data, blockSize, paddingMode);
        byte[] result = new byte[padded.length];
        
        for (int i = 0; i < padded.length; i += blockSize) {
            byte[] block = Arrays.copyOfRange(padded, i, i + blockSize);
            byte[] encrypted = cipher.encrypt(block, key);
            System.arraycopy(encrypted, 0, result, i, blockSize);
        }
        return result;
    }
    
    // CBC - каждый блок XOR с предыдущим зашифрованным
    private byte[] encryptCBC(byte[] data, byte[] key, byte[] iv, PaddingMode paddingMode) {
        int blockSize = cipher.getBlockSize();
        if (iv.length != blockSize) {
            throw new IllegalArgumentException("IV length must match block size.");
        }
        byte[] padded = addPadding(data, blockSize, paddingMode);
        byte[] result = new byte[padded.length];
        byte[] previousBlock = iv.clone();
        
        for (int i = 0; i < padded.length; i += blockSize) {
            byte[] block = Arrays.copyOfRange(padded, i, i + blockSize);
            
            // XOR с предыдущим блоком
            xor(block, previousBlock);
            
            byte[] encrypted = cipher.encrypt(block, key);
            System.arraycopy(encrypted, 0, result, i, blockSize);
            previousBlock = encrypted;
        }
        return result;
    }
    
    // CFB - шифруется предыдущий зашифрованный блок, результат XOR с данными
    private byte[] encryptCFB(byte[] data, byte[] key, byte[] iv) {
        int blockSize = cipher.getBlockSize();
        if (iv.length != blockSize) {
            throw new IllegalArgumentException("IV length must match block size.");
        }
        byte[] result = new byte[data.length];
        byte[] previousCiphertext = iv.clone();

        for (int i = 0; i < data.length; i += blockSize) {
            byte[] keystream = cipher.encrypt(previousCiphertext, key);
            int blockLength = Math.min(blockSize, data.length - i);
            byte[] block = Arrays.copyOfRange(data, i, i + blockLength);

            xor(block, Arrays.copyOf(keystream, blockLength));

            System.arraycopy(block, 0, result, i, blockLength);
            previousCiphertext = block;
        }
        return result;
    }

    // OFB - шифруется IV, затем результат предыдущего шифрования
    private byte[] encryptOFB(byte[] data, byte[] key, byte[] iv) {
        int blockSize = cipher.getBlockSize();
        if (iv.length != blockSize) {
            throw new IllegalArgumentException("IV length must match block size.");
        }
        byte[] result = new byte[data.length];
        byte[] keystreamBlock = iv.clone();

        for (int i = 0; i < data.length; i += blockSize) {
            keystreamBlock = cipher.encrypt(keystreamBlock, key);
            int blockLength = Math.min(blockSize, data.length - i);

            for (int j = 0; j < blockLength; j++) {
                result[i + j] = (byte) (data[i + j] ^ keystreamBlock[j]);
            }
        }
        return result;
    }

    // CTR - шифруется счетчик, результат XOR с данными
    private byte[] encryptCTR(byte[] data, byte[] key, byte[] nonce) {
        int blockSize = cipher.getBlockSize();
        if (nonce.length != blockSize) {
            throw new IllegalArgumentException("Nonce length must match block size.");
        }
        byte[] result = new byte[data.length];
        byte[] counter = nonce.clone();
        
        for (int i = 0; i < data.length; i += blockSize) {
            byte[] keystream = cipher.encrypt(counter, key);
            int blockLength = Math.min(blockSize, data.length - i);
            
            for (int j = 0; j < blockLength; j++) {
                result[i + j] = (byte) (data[i + j] ^ keystream[j]);
            }
            incrementCounter(counter);
        }
        return result;
    }
    
    // Реализация различных режимов набивки
    private byte[] addPadding(byte[] data, int blockSize, PaddingMode paddingMode) {
        int paddingLength = blockSize - (data.length % blockSize);
        if (paddingLength == 0) {
            paddingLength = blockSize;
        }
        byte[] padded = new byte[data.length + paddingLength];
        System.arraycopy(data, 0, padded, 0, data.length);
        
        switch (paddingMode) {
            case PKCS7:
                for (int i = data.length; i < padded.length; i++) {
                    padded[i] = (byte) paddingLength;
                }
                break;
            case ZEROS:
                // Нули уже установлены по умолчанию
                break;
            case ANSI_X923:
                padded[padded.length - 1] = (byte) paddingLength;
                break;
            case ISO_10126:
                for (int i = data.length; i < padded.length - 1; i++) {
                    padded[i] = (byte) random.nextInt(256);
                }
                padded[padded.length - 1] = (byte) paddingLength;
                break;
        }
        return padded;
    }

    // Удаление различных режимов набивки
    private byte[] removePadding(byte[] data, PaddingMode paddingMode) {
        if (data.length == 0) {
            return new byte[0];
        }
        
        int paddingLength = data[data.length - 1] & 0xFF;
        if (paddingLength > data.length || paddingLength == 0) {
            return data;
        }
        
        switch (paddingMode) {
            case PKCS7:
                for (int i = 1; i <= paddingLength; i++) {
                    if (data[data.length - i] != (byte) paddingLength) {
                        return data;
                    }
                }
                break;
            case ZEROS:
            case ANSI_X923:
            case ISO_10126:
                // Для этих режимов просто удаляем последние paddingLength байт
                break;
        }
        return Arrays.copyOfRange(data, 0, data.length - paddingLength);
    }
    
    // Инкремент счетчика
    private void incrementCounter(byte[] counter) {
        for (int i = counter.length - 1; i >= 0; i--) {
            counter[i]++;
            if (counter[i] != 0) break;
        }
    }

    // Вспомогательный метод для XOR
    private void xor(byte[] a, byte[] b) {
        for (int i = 0; i < a.length; i++) {
            a[i] ^= b[i];
        }
    }
    
    // Заглушки для остальных режимов
    private byte[] encryptPCBC(byte[] data, byte[] key, byte[] iv, PaddingMode paddingMode) {
        int blockSize = cipher.getBlockSize();
        if (iv.length != blockSize) {
            throw new IllegalArgumentException("IV length must match block size.");
        }
        byte[] padded = addPadding(data, blockSize, paddingMode);
        byte[] result = new byte[padded.length];

        byte[] prevCipherBlock = iv.clone();
        byte[] prevPlainBlock = new byte[blockSize]; // Initial previous plaintext is all zeros

        for (int i = 0; i < padded.length; i += blockSize) {
            byte[] currentPlainBlock = Arrays.copyOfRange(padded, i, i + blockSize);
            byte[] temp = currentPlainBlock.clone();

            xor(currentPlainBlock, prevCipherBlock);
            xor(currentPlainBlock, prevPlainBlock);

            byte[] encrypted = cipher.encrypt(currentPlainBlock, key);
            System.arraycopy(encrypted, 0, result, i, blockSize);

            prevCipherBlock = encrypted;
            prevPlainBlock = temp;
        }
        return result;
    }
    
    private byte[] encryptRandomDelta(byte[] data, byte[] key, byte[] iv) {
        // Примечание: Random Delta - это нестандартный режим. Его реализация
        // требует дополнительного протокола для обмена случайными "дельтами".
        // Без четкой спецификации оставляем заглушку.
        throw new UnsupportedOperationException("Random Delta not implemented yet");
    }
    
    // Методы дешифрования
    private byte[] decryptECB(byte[] data, byte[] key, PaddingMode paddingMode) {
        int blockSize = cipher.getBlockSize();
        byte[] result = new byte[data.length];

        for (int i = 0; i < data.length; i += blockSize) {
            byte[] block = Arrays.copyOfRange(data, i, i + blockSize);
            byte[] decrypted = cipher.decrypt(block, key);
            System.arraycopy(decrypted, 0, result, i, blockSize);
        }
        return removePadding(result, paddingMode);
    }
    
    private byte[] decryptCBC(byte[] data, byte[] key, byte[] iv, PaddingMode paddingMode) {
        int blockSize = cipher.getBlockSize();
        if (iv.length != blockSize) {
            throw new IllegalArgumentException("IV length must match block size.");
        }
        byte[] result = new byte[data.length];
        byte[] previousBlock = iv.clone();

        for (int i = 0; i < data.length; i += blockSize) {
            byte[] block = Arrays.copyOfRange(data, i, i + blockSize);
            byte[] decrypted = cipher.decrypt(block, key);

            // XOR с предыдущим зашифрованным блоком
            xor(decrypted, previousBlock);

            System.arraycopy(decrypted, 0, result, i, blockSize);
            previousBlock = block;
        }
        return removePadding(result, paddingMode);
    }
    
    private byte[] decryptPCBC(byte[] data, byte[] key, byte[] iv, PaddingMode paddingMode) {
        int blockSize = cipher.getBlockSize();
        if (iv.length != blockSize) {
            throw new IllegalArgumentException("IV length must match block size.");
        }
        byte[] result = new byte[data.length];

        byte[] prevCipherBlock = iv.clone();
        byte[] prevPlainBlock = new byte[blockSize]; // Initial previous plaintext is all zeros

        for (int i = 0; i < data.length; i += blockSize) {
            byte[] currentCipherBlock = Arrays.copyOfRange(data, i, i + blockSize);
            byte[] decrypted = cipher.decrypt(currentCipherBlock, key);

            xor(decrypted, prevCipherBlock);
            xor(decrypted, prevPlainBlock);

            System.arraycopy(decrypted, 0, result, i, blockSize);

            prevCipherBlock = currentCipherBlock;
            prevPlainBlock = decrypted;
        }
        return removePadding(result, paddingMode);
    }
    
    private byte[] decryptCFB(byte[] data, byte[] key, byte[] iv) {
        int blockSize = cipher.getBlockSize();
        if (iv.length != blockSize) {
            throw new IllegalArgumentException("IV length must match block size.");
        }
        byte[] result = new byte[data.length];
        byte[] previousCiphertext = iv.clone();

        for (int i = 0; i < data.length; i += blockSize) {
            byte[] keystream = cipher.encrypt(previousCiphertext, key);
            int blockLength = Math.min(blockSize, data.length - i);
            byte[] block = Arrays.copyOfRange(data, i, i + blockLength);

            previousCiphertext = block.clone();
            xor(block, Arrays.copyOf(keystream, blockLength));

            System.arraycopy(block, 0, result, i, blockLength);
        }
        return result;
    }
    
    private byte[] decryptOFB(byte[] data, byte[] key, byte[] iv) {
        // OFB дешифрование = шифрование
        return encryptOFB(data, key, iv);
    }
    
    private byte[] decryptCTR(byte[] data, byte[] key, byte[] iv) {
        // CTR дешифрование = шифрование
        return encryptCTR(data, key, iv);
    }
    
    private byte[] decryptRandomDelta(byte[] data, byte[] key, byte[] iv) {
        // Примечание: Random Delta - это нестандартный режим. Его реализация
        // требует дополнительного протокола для обмена случайными "дельтами".
        // Без четкой спецификации оставляем заглушку.
        throw new UnsupportedOperationException("Random Delta decryption not implemented yet");
    }
}
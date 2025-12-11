package com.example.crypto_project.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Реализация протокола обмена ключами Диффи-Хеллмана.
 * Этот класс представляет одну сторону (участника) в обмене.
 */
public class DiffieHellman {

    private final BigInteger p; // Простое число (модуль)
    private final BigInteger g; // Генератор (первообразный корень по модулю p)

    private final BigInteger privateKey; // Секретный ключ (случайное число)
    private final BigInteger publicKey;  // Публичный ключ, вычисляемый на основе приватного

    /**
     * Конструктор для инициализации участника с заданными параметрами p и g.
     * Генерирует приватный и публичный ключи.
     *
     * @param p Простое число p.
     * @param g Генератор g.
     */
    public DiffieHellman(BigInteger p, BigInteger g) {
        this.p = p;
        this.g = g;

        // Генерируем случайный приватный ключ в диапазоне [1, p-2]
        // Битность ключа должна быть достаточной для безопасности.
        int bitLength = p.bitLength() - 1;
        SecureRandom random = new SecureRandom();
        this.privateKey = new BigInteger(bitLength, random).add(BigInteger.ONE);

        // Вычисляем публичный ключ: publicKey = g^privateKey mod p
        this.publicKey = g.modPow(privateKey, p);
    }

    /**
     * Возвращает публичный ключ этого участника.
     * Этот ключ можно безопасно передавать другой стороне.
     *
     * @return Публичный ключ.
     */
    public BigInteger getPublicKey() {
        return publicKey;
    }

    /**
     * Вычисляет общий секретный ключ на основе публичного ключа другой стороны.
     *
     * @param otherPartyPublicKey Публичный ключ другого участника.
     * @return Общий секретный ключ в виде массива байт.
     */
    public byte[] computeSharedSecret(BigInteger otherPartyPublicKey) {
        if (otherPartyPublicKey.compareTo(BigInteger.ONE) <= 0 || otherPartyPublicKey.compareTo(p.subtract(BigInteger.ONE)) >= 0) {
            throw new IllegalArgumentException("Invalid public key received.");
        }

        // Вычисляем общий секрет: sharedSecret = otherPartyPublicKey^privateKey mod p
        BigInteger sharedSecret = otherPartyPublicKey.modPow(privateKey, p);

        // Возвращаем хэш от секрета, чтобы получить ключ фиксированной длины (например, 256 бит)
        // Это хорошая практика (KDF - Key Derivation Function). Для простоты пока вернем toByteArray().
        return sharedSecret.toByteArray();
    }
}
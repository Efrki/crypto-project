package com.example.crypto_project.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DiffieHellmanTest {

    @Test
    @DisplayName("Two parties should compute the same shared secret")
    void testSharedSecretComputation() {
        // Шаг 1: Согласование общедоступных параметров p и g.
        // В реальном приложении они должны быть большими и криптографически стойкими.
        // Здесь используются небольшие числа для примера.
        // Это число p из RFC 3526 (2048-bit MODP Group)
        BigInteger p = new BigInteger(
            "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
            "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
            "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
            "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
            "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
            "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" +
            "83655D23DCA3AD961C62F356208552BB9ED529077096966D" +
            "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B" +
            "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9" +
            "DE2BCBF6955817183995497CEA956AE515D2261898FA0510" +
            "15728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64" +
            "ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7" +
            "ABF5AE8CDB0933D71E8C94E04A25619DCE3352E0C381F12E" +
            "DE3174F728F67398855728925A07B9B3410B35F41BF27702" +
            "106A024D63A3285E0894385B5B42E4F6", 16);
        BigInteger g = BigInteger.valueOf(2);

        // Шаг 2: Каждая сторона создает свой экземпляр и генерирует ключи.
        DiffieHellman alice = new DiffieHellman(p, g);
        DiffieHellman bob = new DiffieHellman(p, g);

        // Шаг 3: Стороны обмениваются публичными ключами.
        BigInteger alicesPublicKey = alice.getPublicKey();
        BigInteger bobsPublicKey = bob.getPublicKey();

        // Шаг 4: Каждая сторона вычисляет общий секрет, используя свой приватный ключ
        // и публичный ключ другой стороны.
        byte[] alicesSharedSecret = alice.computeSharedSecret(bobsPublicKey);
        byte[] bobsSharedSecret = bob.computeSharedSecret(alicesPublicKey);

        // Шаг 5: Проверяем, что секреты совпадают.
        assertNotNull(alicesSharedSecret);
        assertNotNull(bobsSharedSecret);
        assertArrayEquals(alicesSharedSecret, bobsSharedSecret, "Shared secrets must be identical!");
    }
}
package com.example.crypto_project.crypto;

/**
 * где b = 128/192/256
*/
public class RC6 implements ISymmetricCipher {

    private static final int R = 20;
    private static final int BLOCK_SIZE_BYTES = 16;

    private static final int P32 = 0xB7E15163;
    private static final int Q32 = 0x9E3779B9;

    @Override
    public byte[] encrypt(byte[] data, byte[] key) {
        if (data.length != BLOCK_SIZE_BYTES) {
            throw new IllegalArgumentException("Data block size must be " + BLOCK_SIZE_BYTES + " bytes for RC6");
        }

        int[] S = generateRoundKeys(key); 

        int A = (data[0] & 0xFF) | ((data[1] & 0xFF) << 8) | ((data[2] & 0xFF) << 16) | ((data[3] & 0xFF) << 24);
        int B = (data[4] & 0xFF) | ((data[5] & 0xFF) << 8) | ((data[6] & 0xFF) << 16) | ((data[7] & 0xFF) << 24);
        int C = (data[8] & 0xFF) | ((data[9] & 0xFF) << 8) | ((data[10] & 0xFF) << 16) | ((data[11] & 0xFF) << 24);
        int D = (data[12] & 0xFF) | ((data[13] & 0xFF) << 8) | ((data[14] & 0xFF) << 16) | ((data[15] & 0xFF) << 24);

        B = B + S[0];
        D = D + S[1];
        for (int i = 0; i < R; i++) {
            int t = Integer.rotateLeft(B * (2 * B + 1), 5);
            int u = Integer.rotateLeft(D * (2 * D + 1), 5);

            A = Integer.rotateLeft(A ^ t, u) + S[2 * i + 2];
            C = Integer.rotateLeft(C ^ u, t) + S[2 * i + 3];

            int temp = A;
            A = B;
            B = C;
            C = D; 
            D = temp;
        }
        A = A + S[2 * R + 2];
        C = C + S[2 * R + 3];

        byte[] encryptedData = new byte[BLOCK_SIZE_BYTES];
        encryptedData[0] = (byte) A;
        encryptedData[1] = (byte) (A >> 8);
        encryptedData[2] = (byte) (A >> 16);
        encryptedData[3] = (byte) (A >> 24);

        encryptedData[4] = (byte) B;
        encryptedData[5] = (byte) (B >> 8);
        encryptedData[6] = (byte) (B >> 16);
        encryptedData[7] = (byte) (B >> 24);

        encryptedData[8] = (byte) C;
        encryptedData[9] = (byte) (C >> 8);
        encryptedData[10] = (byte) (C >> 16);
        encryptedData[11] = (byte) (C >> 24);

        encryptedData[12] = (byte) D;
        encryptedData[13] = (byte) (D >> 8);
        encryptedData[14] = (byte) (D >> 16);
        encryptedData[15] = (byte) (D >> 24);

        return encryptedData;

    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        if (data.length != BLOCK_SIZE_BYTES) {
            throw new IllegalArgumentException("Data block size must be " + BLOCK_SIZE_BYTES + " bytes for RC6.");
        }

        int[] S = generateRoundKeys(key);

        int A = (data[0] & 0xFF) | ((data[1] & 0xFF) << 8) | ((data[2] & 0xFF) << 16) | ((data[3] & 0xFF) << 24);
        int B = (data[4] & 0xFF) | ((data[5] & 0xFF) << 8) | ((data[6] & 0xFF) << 16) | ((data[7] & 0xFF) << 24);
        int C = (data[8] & 0xFF) | ((data[9] & 0xFF) << 8) | ((data[10] & 0xFF) << 16) | ((data[11] & 0xFF) << 24);
        int D = (data[12] & 0xFF) | ((data[13] & 0xFF) << 8) | ((data[14] & 0xFF) << 16) | ((data[15] & 0xFF) << 24);

        C = C - S[2 * R + 3];
        A = A - S[2 * R + 2];

        for (int i = R - 1; i >= 0; i--) {
            int temp = D;
            D = C;
            C = B;
            B = A;
            A = temp;

            int t = Integer.rotateLeft(B * (2 * B + 1), 5);
            int u = Integer.rotateLeft(D * (2 * D + 1), 5);

            C = Integer.rotateRight(C - S[2 * i + 3], t) ^ u;
            A = Integer.rotateRight(A - S[2 * i + 2], u) ^ t;
        }

        D = D - S[1];
        B = B - S[0];

        byte[] decryptedData = new byte[BLOCK_SIZE_BYTES];
        decryptedData[0] = (byte) A;
        decryptedData[1] = (byte) (A >> 8);
        decryptedData[2] = (byte) (A >> 16);
        decryptedData[3] = (byte) (A >> 24);

        decryptedData[4] = (byte) B;
        decryptedData[5] = (byte) (B >> 8);
        decryptedData[6] = (byte) (B >> 16);
        decryptedData[7] = (byte) (B >> 24);

        decryptedData[8] = (byte) C;
        decryptedData[9] = (byte) (C >> 8);
        decryptedData[10] = (byte) (C >> 16);
        decryptedData[11] = (byte) (C >> 24);

        decryptedData[12] = (byte) D;
        decryptedData[13] = (byte) (D >> 8);
        decryptedData[14] = (byte) (D >> 16);
        decryptedData[15] = (byte) (D >> 24);

        return decryptedData;
    }

    public int getBlockSize() {
        return BLOCK_SIZE_BYTES;
    }

    private int[] generateRoundKeys(byte[] key) {
        int c = key.length / 4;
        int[] L = new int[c];
        for (int i = 0; i < c; i++){
            L[i] = (key[4*i] & 0xFF) | ((key[4*i+1] & 0xFF) << 8) | ((key[4*i+2] & 0xFF) << 16) | ((key[4*i+3] & 0xFF) << 24);
        }
        int sLength = 2 * R + 4;
        int[] S = new int[sLength];
        S[0] = P32;
        for (int i = 1; i < sLength; i++) {
            S[i] = S[i-1] + Q32;
        }
        int i = 0, j = 0;
        int A = 0, B = 0;
        for (int h = 0; h < 3 * Math.max(c, sLength); h++) {
            A = S[i] = Integer.rotateLeft(S[i] + A + B, 3);
            B = L[j] = Integer.rotateLeft(L[j] + A + B, A + B);
            i = (i + 1) % sLength;
            j = (j + 1) % c;
        }
        return S;
    }
}

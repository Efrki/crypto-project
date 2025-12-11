package com.example.crypto_project.crypto;

public enum CipherMode {
    ECB,    // Electronic Codebook
    CBC,    // Cipher Block Chaining
    PCBC,   // Propagating Cipher Block Chaining
    CFB,    // Cipher Feedback
    OFB,    // Output Feedback
    CTR,    // Counter
    RANDOM_DELTA // Random Delta
}
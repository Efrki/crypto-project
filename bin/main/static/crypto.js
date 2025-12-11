// Diffie-Hellman implementation
const DiffieHellman = {
    // RFC 3526 2048-bit MODP Group
    P: "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A0864D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E208E24FA074E5AB3143DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF",
    G: "2",

    generateKeys: function() {
        const p = BigInt("0x" + this.P);
        const g = BigInt(this.G);
        
        // Generate random private key (256 bits for simplicity)
        const privateKey = this._generateRandomBigInt(256);
        
        // Calculate public key: g^privateKey mod p
        const publicKey = this._modPow(g, privateKey, p);
        
        return {
            privateKey: privateKey.toString(16),
            publicKey: publicKey.toString(16)
        };
    },

    computeSharedSecret: function(privateKeyHex, otherPublicKeyHex) {
        const p = BigInt("0x" + this.P);
        const privateKey = BigInt("0x" + privateKeyHex);
        const otherPublicKey = BigInt("0x" + otherPublicKeyHex);
        
        // Calculate shared secret: otherPublicKey^privateKey mod p
        const sharedSecret = this._modPow(otherPublicKey, privateKey, p);
        
        // Convert to hex and take first 32 bytes (256 bits) for key
        let secretHex = sharedSecret.toString(16);
        if (secretHex.length > 64) {
            secretHex = secretHex.substring(0, 64);
        } else {
            secretHex = secretHex.padStart(64, '0');
        }
        
        return secretHex;
    },

    _modPow: function(base, exponent, modulus) {
        let result = BigInt(1);
        base = base % modulus;
        
        while (exponent > 0) {
            if (exponent % BigInt(2) === BigInt(1)) {
                result = (result * base) % modulus;
            }
            exponent = exponent / BigInt(2);
            base = (base * base) % modulus;
        }
        
        return result;
    },

    _generateRandomBigInt: function(bits) {
        const bytes = Math.ceil(bits / 8);
        const randomBytes = new Uint8Array(bytes);
        crypto.getRandomValues(randomBytes);
        
        let hex = '';
        for (let i = 0; i < randomBytes.length; i++) {
            hex += randomBytes[i].toString(16).padStart(2, '0');
        }
        
        return BigInt("0x" + hex);
    }
};

// RC6 implementation (simplified for client-side)
const RC6 = {
    encrypt: function(plaintext, keyHex, ivHex) {
        // For now, return placeholder - full implementation would be complex
        // In production, use Web Crypto API or a library
        return this._hexEncode(plaintext);
    },

    decrypt: function(ciphertext, keyHex, ivHex) {
        // For now, return placeholder
        return this._hexDecode(ciphertext);
    },

    _hexEncode: function(str) {
        let hex = '';
        for (let i = 0; i < str.length; i++) {
            hex += str.charCodeAt(i).toString(16).padStart(2, '0');
        }
        return hex;
    },

    _hexDecode: function(hex) {
        let str = '';
        for (let i = 0; i < hex.length; i += 2) {
            str += String.fromCharCode(parseInt(hex.substr(i, 2), 16));
        }
        return str;
    }
};

// Copyright OpenX Limited 2010. All Rights Reserved.
package org.openx.market.ssrtb.crypter;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * A class that can encrypt and decrypt the price macro used in
 * Server-Side Real-time bidders.
 */
public class PriceDecrypt {
  private static final Charset US_ASCII = Charset.forName("US-ASCII");
  private static final String HMAC_SHA1 = "HmacSHA1";

  private static final int IV_SIZE = 16;
  private static final int CIPHERTEXT_SIZE = 8;
  private static final int INTEGRITY_SIZE = 4;

  public static SecretKey b64StrToKey(String b64Str) {
	  byte[] keyBytes = Base64.decodeBase64(b64Str.getBytes(US_ASCII));
	  return new SecretKeySpec(keyBytes, HMAC_SHA1);
  }

  public static SecretKeySpec getKeySpec(String key) throws Exception {
    String keyStr = key;
    byte[] keyBytes = null;
    if (keyStr.length() == 44) {
      keyBytes = Base64.decodeBase64(keyStr.getBytes("US-ASCII"));
    } else if (keyStr.length() == 64) {
      keyBytes = Hex.decodeHex(keyStr.toCharArray());
    }

    return new SecretKeySpec(keyBytes, "HmacSHA1");
  }
  
  public static long decodeDecrypt(String base64Websafe, String encryptKey, String integrityKey) throws SsRtbDecryptingException {
    SecretKeySpec s_encryptKey = null;
    SecretKeySpec s_integrityKey = null;
    try {
      s_encryptKey = PriceDecrypt.getKeySpec( encryptKey );
      s_integrityKey = PriceDecrypt.getKeySpec( integrityKey );
    } catch (Exception e) {
    }
    
	  String base64NonWebsafe = base64Websafe.replace("-", "+").replace("_", "/") + "==";
	  byte[] encrypted = Base64.decodeBase64(base64NonWebsafe.getBytes(US_ASCII));
	  byte[] decrypted = PriceDecrypt.decrypt(encrypted, s_encryptKey, s_integrityKey);
	  return PriceDecrypt.toLong(decrypted);
  }

  public static byte[] decrypt(byte[] ciphered, String b64EncryptKey, String b64IntegrityKey) throws SsRtbDecryptingException {
	  return decrypt(ciphered, PriceDecrypt.b64StrToKey(b64EncryptKey), PriceDecrypt.b64StrToKey(b64IntegrityKey));
  }
  
  public static byte[] decrypt(byte[] crypted, SecretKey encryptionKey, SecretKey integrityKey) throws SsRtbDecryptingException {
  	// Create array to store unciphered value
	  byte[] unciphered = new byte[CIPHERTEXT_SIZE];
	    
	  try {
	  	// Create encrypting MAC
	    Mac mac = Mac.getInstance(HMAC_SHA1);
	    mac.init(encryptionKey);
	      
	    // Create keypad
	    mac.update(crypted, 0, IV_SIZE);
	    byte[] pad = mac.doFinal();
	      
	    // XOR values to get unciphered value
	    for (int i = 0; i < CIPHERTEXT_SIZE; i++) {
	    	unciphered[i] = (byte)(pad[i] ^ crypted[IV_SIZE + i]);
	    }

	    // Calculate signature bytes
	    mac.init(integrityKey);
	    mac.update(unciphered);
	    mac.update(crypted, 0, IV_SIZE);
	    byte[] signature = mac.doFinal();
	      
	    // Check signature
	    for (int i = 0; i < INTEGRITY_SIZE; i++) {
	    	if (signature[i] != crypted[IV_SIZE + CIPHERTEXT_SIZE + i]) {
	    		throw new SsRtbDecryptingException("Signature does not match.", null);
	      }
	    }
	   } catch (NoSuchAlgorithmException e) {
	  	 throw new RuntimeException("HmacSHA1 not supported.", e);
	   } catch (InvalidKeyException e) {
	  	 throw new RuntimeException("Key not valid for HmacSHA1", e);
	   }

	  return unciphered;
	}

  /**
   * Converts a long to an 8 byte array. Code is derived from that
   * used in {@link java.io.DataOutputStream#writeLong(long)}.
   * @param val
   * @return byte array of length 8
   */
  protected byte[] toBytes(long val) {
  	byte[] buff = new byte[8];
    buff[0] = (byte)(val >>> 56);
    buff[1] = (byte)(val >>> 48);
    buff[2] = (byte)(val >>> 40);
    buff[3] = (byte)(val >>> 32);
    buff[4] = (byte)(val >>> 24);
    buff[5] = (byte)(val >>> 16);
    buff[6] = (byte)(val >>>  8);
    buff[7] = (byte)(val >>>  0);
    return buff;
  }

  /**
   * Converts an array of 8 bytes into a long. Code is dervied from that
   * used in {@link java.io.DataInputStream#readLong()}.
   * @param buff
   * @return value of the 8 byte array as a long
   */
  protected static long toLong(byte[] buff) {
  	return (((long)buff[0] << 56) +
	  ((long)(buff[2] & 255) << 40) +
	  ((long)(buff[3] & 255) << 32) +
	  ((long)(buff[4] & 255) << 24) +
	  ((buff[5] & 255) << 16) +
	  ((buff[6] & 255) <<  8) +
	  ((buff[7] & 255) <<  0));
	}
}


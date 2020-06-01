/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author 
 */
public final class Crypto {
    public final static Charset utf8=Charset.forName("UTF-8");
    public static KeyStore loadKeyStore(File file,String type,String pswd) throws Exception{
        FileInputStream keyFileStream = null;
        try{
            return loadKeyStore(new FileInputStream(file), type, pswd);
        }finally{
            if(keyFileStream!=null)
                try{keyFileStream.close();}catch(Exception ex){}
        }
    }

    public static KeyStore loadKeyStore(InputStream keyFileStream,String type,String pswd) throws Exception{
        KeyStore ks = KeyStore.getInstance(type);        
        ks.load(keyFileStream, pswd.toCharArray());
        return ks;
    }

    public static String getX509EncodedPublicKey(PublicKey key){
        return Base64.encodeBase64String(key.getEncoded());
    }
    
    public static Certificate loadCertificate(File certificateFile,String certificateType) throws FileNotFoundException, CertificateException{
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(certificateFile);
            return loadCertificate(fileInputStream, certificateType);
        } 
        finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {}
            }
        }        
    }
    
    public static X509Certificate loadX509Certificate(InputStream certificateStream) throws CertificateException{
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(certificateStream);
    }
    
    public static Certificate loadCertificate(InputStream certificateStream,String certificateType) throws CertificateException{
        return CertificateFactory.getInstance(certificateType).generateCertificate(certificateStream);
    }
    
    public static KeyStore.PrivateKeyEntry getEntry(KeyStore keyStore,String keyAlias,String keyPswd) throws Exception{
        if(keyAlias==null)
            keyAlias=keyStore.aliases().nextElement();
        return (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyAlias, keyPswd==null?null:new KeyStore.PasswordProtection(keyPswd.toCharArray()));
    }
    
    public static SecretKey generateAesKey(int size) throws NoSuchAlgorithmException{
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(size);
        return kgen.generateKey();
    }
    
    public static SecretKey generateSecretKey(String algo,int size) throws NoSuchAlgorithmException{
        KeyGenerator kgen = KeyGenerator.getInstance(algo);
        kgen.init(size);
        return kgen.generateKey();        
    }
    
    public static byte[] wrap(PublicKey rsaPublicKey,SecretKey aesKey,boolean scramble) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.WRAP_MODE, rsaPublicKey);
        byte[] wrappedKey=cipher.wrap(aesKey);
        if(scramble)
            return scrambleBytes(wrappedKey);
        return wrappedKey;
    }
    
    public static byte[] wrap(PublicKey rsaPublicKey,SecretKey aesKey) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException{
        return wrap(rsaPublicKey, aesKey, false);
    }
    
    public static SecretKey unwrap(PrivateKey rsaPrivateKey,byte[] wrappedAesKey,boolean scrambled) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.UNWRAP_MODE, rsaPrivateKey);
        if(scrambled)
            wrappedAesKey=descrambleBytes(wrappedAesKey);
        return (SecretKey)cipher.unwrap(wrappedAesKey, "AES", Cipher.SECRET_KEY);
    }
    
    public static SecretKey unwrap(PrivateKey rsaPrivateKey,byte[] wrappedAesKey) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException{
        return unwrap(rsaPrivateKey, wrappedAesKey, false);
    }
    
    public static byte[] encryptScramble(byte[] data,Key encryptionKey,String transformation,String provider,byte[] iv) throws Exception{
        return scrambleBytes(encrypt(data, encryptionKey, transformation, provider, iv));
    }
    
    private static Cipher getCipher(String transformation, String provider) throws Exception{
        if(provider==null){
            return Cipher.getInstance(transformation);
        }
        return Cipher.getInstance(transformation,provider);
    }
    
    private static final int CIPHER_BLOCK_SIZE=128/8;
    private static byte[] resizedIv(byte[] origIv){
        byte[] resizedIv=new byte[CIPHER_BLOCK_SIZE];
        if(resizedIv.length>origIv.length)
            System.arraycopy(origIv, 0, resizedIv, 0, origIv.length);
        else
            System.arraycopy(origIv, 0, resizedIv, 0, resizedIv.length);
        return resizedIv;
    }

    public static byte[] encrypt(byte[] data,Key encryptionKey,String transformation,String provider,byte[] iv) throws Exception{
        Cipher cipher = getCipher(transformation, provider);
        if(iv==null || iv.length==0)
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        else
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey,new IvParameterSpec(resizedIv(iv)));
        return cipher.doFinal(data);
    }
    
    private static final byte[] EMPTY_BYTE_ARRAY=new byte[0];
    private static final byte[] defaultIv={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
    public static byte[] decrypt(byte[] data,Key decryptionKey,String transformation,String provider,byte[] iv) throws Exception{
        if(data==null || data.length<1)
            return EMPTY_BYTE_ARRAY;
        Cipher cipher = getCipher(transformation, provider);
        if(iv==null || iv.length==0)
                cipher.init(Cipher.DECRYPT_MODE, decryptionKey);        
        else
            cipher.init(Cipher.DECRYPT_MODE, decryptionKey,new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }
    
    public static byte[] decryptScramble(byte[] data,Key decryptionKey,String transformation,String provider,byte[] iv) throws Exception{
        return decrypt(descrambleBytes(data), decryptionKey, transformation, provider, iv);
    }
    
    public static byte[] doubleScramble(byte[] bytes){
        return doubleScrambleBytes(bytes);
    }
    
    public static byte[] xor(byte[] bytes){
        return xor(bytes, (byte)101);
    }
            
    public static byte[] xor(byte[] bytes,byte xorWith){
        for(int ctr=0;ctr<bytes.length;ctr++){
            bytes[ctr]=(byte)(bytes[ctr]^xorWith);
        }
        return bytes;        
    }
            
    private static byte[] doubleScrambleBytes(byte[] bytes){
        Random random=new Random(26658877889l);
        byte[] prefix=new byte[10];
        byte[] suffix=new byte[10];
        random.nextBytes(prefix);
        random.nextBytes(suffix);
        byte[] copy=Arrays.copyOf(bytes, bytes.length);
        scrambleBytes(copy);
        byte[] result=new byte[bytes.length+20];                
        int diff=0;
        for(int ctr=0;ctr<prefix.length;ctr++)
            result[ctr+diff]=prefix[ctr];
        diff=prefix.length;
        for(int ctr=0;ctr<copy.length;ctr++)
            result[ctr+diff]=copy[ctr];
        diff=prefix.length+copy.length;
        for(int ctr=0;ctr<suffix.length;ctr++)
            result[ctr+diff]=suffix[ctr];
        return halfScrambleBytes(result);
    }
    
    private static byte[] halfScrambleBytes(byte[] bytes){
        int diff=bytes.length/2;
        if(bytes.length%2>0)
            diff++;
        byte temp;
        for(int ctr=0;ctr<bytes.length/2;ctr++){
            temp=bytes[ctr];
            bytes[ctr]=bytes[ctr+diff];
            bytes[ctr+diff]=temp;
        }
        return bytes;
    }
        
    private static byte[] scrambleBytes(byte[] bytes){
        if(bytes.length<2)
            return bytes;
        byte temp;
        for(int ctr=0;ctr<bytes.length-1;ctr+=2){
            temp=bytes[ctr];
            bytes[ctr]=bytes[ctr+1];
            bytes[ctr+1]=temp;
        }
        int diff=bytes.length/2;
        if(bytes.length%2>0)
            diff++;
        for(int ctr=0;ctr<bytes.length/2;ctr++){
            temp=bytes[ctr];
            bytes[ctr]=bytes[ctr+diff];
            bytes[ctr+diff]=temp;
        }
        return bytes;
    }
    
    public static byte[] doubleDescramble(byte[] bytes){
        return doubleDeScrambleBytes(bytes);
    }
    
    private static byte[] doubleDeScrambleBytes(byte[] bytes){
        halfDescrambleBytes(bytes);
        byte[] result=new byte[bytes.length-20];
        int diff=10;
        for(int ctr=0;ctr<result.length;ctr++)
            result[ctr]=bytes[ctr+diff];
        return descrambleBytes(result);
    }
    
    private static byte[] halfDescrambleBytes(byte[] bytes){
        if(bytes.length<2)
            return bytes;
        byte temp;
        int diff=bytes.length/2;
        if(bytes.length%2>0)
            diff++;
        for(int ctr=0;ctr<bytes.length/2;ctr++){
            temp=bytes[ctr];
            bytes[ctr]=bytes[ctr+diff];
            bytes[ctr+diff]=temp;
        }
        return bytes;
    }
        
    private static byte[] descrambleBytes(byte[] bytes){
        if(bytes==null || bytes.length<2)
            return bytes;
        byte temp;
        int diff=bytes.length/2;
        if(bytes.length%2>0)
            diff++;
        for(int ctr=0;ctr<bytes.length/2;ctr++){
            temp=bytes[ctr];
            bytes[ctr]=bytes[ctr+diff];
            bytes[ctr+diff]=temp;
        }
        for(int ctr=0;ctr<bytes.length-1;ctr+=2){
            temp=bytes[ctr];
            bytes[ctr]=bytes[ctr+1];
            bytes[ctr+1]=temp;
        }
        return bytes;
    }
    
    public static byte[] generateIv(int size){
        byte[] bytes=new byte[size];
        new Random().nextBytes(bytes);
        return bytes;
    }
    
    public static PublicKey loadX509PublicKey(String base64Encodedx509RSAKey) throws NoSuchAlgorithmException, InvalidKeySpecException{
        return loadX509PublicKey(Base64.decodeBase64(base64Encodedx509RSAKey));
    }

    public static PublicKey loadX509PublicKey(byte[] x509RSAKey) throws NoSuchAlgorithmException, InvalidKeySpecException{
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(x509RSAKey));
    }
    
    public static PublicKey loadPublicKey(KeySpec keySpec) throws InvalidKeySpecException, NoSuchAlgorithmException{
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    public static PrivateKey loadPrivateKey(byte[] privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException{
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKey));
    }
    
    public static SecretKey loadSecretKey(byte[] aesKey){
        return new SecretKeySpec(aesKey, 0, aesKey.length,"AES");
    }
    
    public static boolean verifySignature(PublicKey key ,byte[] dataToVerify,byte[] signatureInBytes) throws Exception{
        Signature signature=Signature.getInstance("SHA256withRSA");
        signature.initVerify(key);
        signature.update(dataToVerify);
        return signature.verify(signatureInBytes);
    }
    
    public static byte[] sign(PrivateKey key, byte[] dataToSign) throws Exception{
        Signature signature=Signature.getInstance("SHA256withRSA");
        signature.initSign(key);
        signature.update(dataToSign);
        return signature.sign();
    }
    
    public static byte[] hashSha256(byte[] value){
        return hashSha256(value, null);
    }

    public static byte[] hashSha256(byte[] value,byte[] salt){
        MessageDigest sha256=Crypto.sha256.get();
        sha256.reset();
        if(salt==null || salt.length<1)
            return sha256.digest(value);
        byte[] newBytes = Arrays.copyOf(value, value.length + salt.length);
        System.arraycopy(salt, 0, newBytes, value.length, salt.length);
        return sha256.digest(newBytes);
    }
    
    public static KeyPair generateRsaKeyPair(int keySize) throws NoSuchAlgorithmException{
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();        
    }
    
    public static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException{
        return generateRsaKeyPair(2048);
    }
    
    public static byte[] signHash(byte[] sha246Hash,PrivateKey privateKey) throws Exception{
        byte[] digestPrefix={48, 49, 48, 13, 6, 9, 96, -122, 72, 1, 101, 3, 4, 2, 1, 5, 0, 4, 32};
        byte[] digest=Arrays.copyOf(digestPrefix, 51);
        System.arraycopy(sha246Hash, 0, digest, 19, sha246Hash.length);        
        return encrypt(digest, privateKey, "RSA", null, null);        
    }
    
    /**
     * This functions returns sha256 hash value of the String passed .
     * if addSalt is true ,then we append salt with string passed,before hashing .
     * if addSalt is false then directly hash the string .
     * Value of the hash is case insensitive .
     * 
     * @param value - is string whose hash we have to calculate
     * @param addSalt - boolean value which is used to decide,if we have to add salt or not 
     * @return String - hash value of given string .
     */
    public static String hashSha256(String value,boolean addSalt){
        return hashSha256(value, addSalt, true);
    }
    
    /**
     * This functions returns sha256 hash value of the String passed .
     * if addSalt is true ,then we append salt with string passed,before hashing .
     * if addSalt is false then directly hash the string . 
     * if caseInsensitive is true then convert the String passed to lower case before hashing it .
     * if caseInsensitive is false then directly hash the passed string .
     * @param s -is string whose hash we have to calculate
     * @param addSalt - boolean value which is used to decide,if we have to add salt or not 
     * @param caseInsensitive -boolean value which is used to decide,if the hash is case sensitive or not .
     * @return String - hash value of given string .
     */
    public static String hashSha256(String s,boolean addSalt, boolean caseInsensitive){
        String value = s;
        if(caseInsensitive)
            value = value.toLowerCase();
        if(addSalt)
            return Base64.encodeBase64String(Crypto.hashSha256(value.getBytes(),generateSalt(value).getBytes()));
        return Base64.encodeBase64String(Crypto.hashSha256(value.getBytes(),null));
    }
    
    
    private static String generateSalt(String value){
        char[] chars={'h','\0','s','1'};
        int length=value.length();
        if(length<2){
            chars[1]=value.charAt(0);
            chars[2]=chars[1];
            return new String(chars);
        }         
        chars[1]=(char)((value.charAt(0) + value.charAt(length-1))%90+32);
        if(length<3)
            chars[2]=chars[1];
        else
            chars[2]=(char)((value.charAt(length/2) + value.charAt(length/2+1))%90+32);        
        return new String(chars);
    }
    
//  ------------------------ Thread Local ------------------------
    private static final ThreadLocal<MessageDigest> sha256 = 
            new ThreadLocal<MessageDigest>(){
                @Override protected MessageDigest initialValue() {
                    try {
                        return MessageDigest.getInstance("SHA-256");
                    } catch (NoSuchAlgorithmException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
//  ------------------------ End Thread Local ------------------------    
    
//    public static byte[] hashSha1(byte[] value) throws NoSuchAlgorithmException{
//        return hashSha1(value, null);
//    }
//           
//    public static byte[] hashSha1(byte[] value, byte[] salt) throws NoSuchAlgorithmException{
//        MessageDigest sha1=MessageDigest.getInstance("SHA-1");
//        sha1.reset();
//        if(salt==null || salt.length<1)
//            return sha1.digest(value);
//        byte[] newBytes = Arrays.copyOf(value, value.length + salt.length);
//        System.arraycopy(salt, 0, newBytes, value.length, salt.length);
//        return sha1.digest(newBytes);
//    }
    
}

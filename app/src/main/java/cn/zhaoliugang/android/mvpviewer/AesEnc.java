package cn.zhaoliugang.android.mvpviewer;


import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class AesEnc {
    public static byte[] aesEncrypt(byte[] source, byte rawKeyData[])
            throws GeneralSecurityException {
        // 处理密钥
        SecretKeySpec key = new SecretKeySpec(rawKeyData, "AES");
        // 加密
        //Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        //Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        //cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(getIV()));
        return cipher.doFinal(source);
    }
    public static byte[] aesDecrypt(byte[] data, byte rawKeyData[])
            throws GeneralSecurityException {
        // 处理密钥
        SecretKeySpec key = new SecretKeySpec(rawKeyData, "AES");
        // 解密
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }
    protected static byte[] getIV() {
        String iv = "1234567812345678"; //IV length: must be 16 bytes long
        return iv.getBytes();
    }
    public static void main(String[] args) throws Exception {
        // AES算法要求密鈅128位(16字节)
        byte rawKeyData[] = "0123456789abcdef".getBytes();
        System.out.println("key:" + ByteArrayToHexString(rawKeyData));

        String so = "my_test_data0001123456781234    ";
        byte[] source = so.getBytes();
        System.out.println("str:" + so );
        System.out.println("bin:" + ByteArrayToHexString(source) );
        // 加密
        byte[] enc = aesEncrypt(source, rawKeyData);

        String st = ByteArrayToHexString(enc);
        System.out.println("enc:" + st);

        // 解密
        byte[] dec = aesDecrypt(enc, rawKeyData);
        System.out.println("dec:" + new String(dec));

    }
    public static byte[] HexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}

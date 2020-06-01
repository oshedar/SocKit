/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

/**
 *
 * @author Hoshedar Irani
 */
class Functions {
    
    static byte[] scrambleBytes(byte[] bytes){
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
        
    static byte[] descrambleBytes(byte[] bytes){
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
    
    static int toInt(byte[] bytes){ 
        return (((bytes[0]&0xFF)<<24) |  ((bytes[1]&0xFF)<<16) | ((bytes[2]&0xFF)<<8) | ((bytes[3]&0xFF)) ) ;
    }
    
    static byte[] intToBytes(int value){
        byte[] bytes=new byte[4];
        bytes[3]=(byte)(value & 0xFF);
        bytes[2]=(byte)((value>>>8) & 0xFF);
        bytes[1]=(byte)((value>>>16) & 0xFF);
        bytes[0]=(byte)((value>>>24) & 0xFF);
        return bytes;
    }
    
    static byte[] longToBytes(long value){
        byte[] bytes=new byte[8];
        bytes[7]=(byte)(value & 0xFF);
        bytes[6]=(byte)((value>>>8) & 0xFF);
        bytes[5]=(byte)((value>>>16) & 0xFF);
        bytes[4]=(byte)((value>>>24) & 0xFF);
        bytes[3]=(byte)((value>>>32) & 0xFF);
        bytes[2]=(byte)((value>>>40) & 0xFF);
        bytes[1]=(byte)((value>>>48) & 0xFF);
        bytes[0]=(byte)((value>>>56) & 0xFF);
        return bytes;
    }
    
    private static final int DEFAULT_SEED = 104729; 
    private static final long C1 = 0x87c37b91114253d5L;
    private static final long C2 = 0x4cf5ad432745937fL;
    private static final int R1 = 31;
    private static final int R2 = 27;
    private static final int R3 = 33;
    private static final int M = 5;
    private static final int N1 = 0x52dce729;
    private static final int N2 = 0x38495ab5;

    static long hash64(byte[] data) {
      return hash64(data, 0, data.length, DEFAULT_SEED);
    }

    private static long fmix64(long h) {
      h ^= (h >>> 33);
      h *= 0xff51afd7ed558ccdL;
      h ^= (h >>> 33);
      h *= 0xc4ceb9fe1a85ec53L;
      h ^= (h >>> 33);
      return h;
    }
    
    private static long hash64(byte[] data, int offset, int length, int seed) {
      long hash = seed;
      final int nblocks = length >> 3;

      // body
      for (int i = 0; i < nblocks; i++) {
        final int i8 = i << 3;
        long k = ((long) data[offset + i8] & 0xff)
            | (((long) data[offset + i8 + 1] & 0xff) << 8)
            | (((long) data[offset + i8 + 2] & 0xff) << 16)
            | (((long) data[offset + i8 + 3] & 0xff) << 24)
            | (((long) data[offset + i8 + 4] & 0xff) << 32)
            | (((long) data[offset + i8 + 5] & 0xff) << 40)
            | (((long) data[offset + i8 + 6] & 0xff) << 48)
            | (((long) data[offset + i8 + 7] & 0xff) << 56);

        // mix functions
        k *= C1;
        k = Long.rotateLeft(k, R1);
        k *= C2;
        hash ^= k;
        hash = Long.rotateLeft(hash, R2) * M + N1;
      }

      // tail
      long k1 = 0;
      int tailStart = nblocks << 3;
      switch (length - tailStart) {
        case 7:
          k1 ^= ((long) data[offset + tailStart + 6] & 0xff) << 48;
        case 6:
          k1 ^= ((long) data[offset + tailStart + 5] & 0xff) << 40;
        case 5:
          k1 ^= ((long) data[offset + tailStart + 4] & 0xff) << 32;
        case 4:
          k1 ^= ((long) data[offset + tailStart + 3] & 0xff) << 24;
        case 3:
          k1 ^= ((long) data[offset + tailStart + 2] & 0xff) << 16;
        case 2:
          k1 ^= ((long) data[offset + tailStart + 1] & 0xff) << 8;
        case 1:
          k1 ^= ((long) data[offset + tailStart] & 0xff);
          k1 *= C1;
          k1 = Long.rotateLeft(k1, R1);
          k1 *= C2;
          hash ^= k1;
      }

      // finalization
      hash ^= length;
      hash = fmix64(hash);

      return hash;
    }
    
    static byte[] concat(byte[]...arrays){
        // Determine the length of the result array
        int totalLength = 0;
        for(int i = 0; i < arrays.length; i++)
        {
            totalLength += arrays[i].length;
        }

        // create the result array
        byte[] result = new byte[totalLength];

        // copy the source arrays into the result array
        int currentIndex = 0;
        for(int i = 0; i < arrays.length; i++)
        {
            System.arraycopy(arrays[i], 0, result, currentIndex, arrays[i].length);
            currentIndex += arrays[i].length;
        }

        return result;
    }
}

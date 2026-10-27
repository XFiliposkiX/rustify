package dev.rustify;
import java.nio.file.*;import java.util.*;
public final class IntegrationTest {
 public static void main(String[] args)throws Exception {
  Path dir=Files.createTempDirectory("rustify-test-");Engine.initialize(dir,"test","test","test");
  boolean expected=args.length>0 && args[0].equals("native");if(Engine.status().contains("nativeLoaded=true")!=expected)throw new AssertionError(Engine.status());
  Random r=new Random(42);for(int bits=1;bits<=32;bits++)for(int n:new int[]{0,1,63,64,65,4096}){int[] v=new int[n];for(int i=0;i<n;i++)v[i]=r.nextInt() >>> (32-bits);if(!Arrays.equals(Engine.referencePack(v,bits),Engine.experimentalPack(v,bits)))throw new AssertionError("cross-language mismatch");}
  if(expected) {for(int bits:new int[]{0,33}) {try{NativeBridge.pack(new int[]{1},bits);throw new AssertionError("accepted invalid");}catch(IllegalArgumentException ok){}}try{NativeBridge.pack(new int[]{2},1);throw new AssertionError("accepted overflow");}catch(IllegalArgumentException ok){}}
  System.out.println(Engine.status());for(int i=0;i<5;i++){System.out.println(Engine.benchmark("jni"));System.out.println(Engine.benchmark("chunks"));}
  if(!Files.readString(dir.resolve("rustify/benchmarks/latest.json")).contains("unobserved"))throw new AssertionError("profile missing");
 }
}

package dev.olive.utils;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class ClassLoaderDetection {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws IllegalClassFormatException {
                if (loader != null && !loader.getClass().getName().startsWith("java")) {
                    System.out.println("检测到异常的类加载器，可能存在反编译风险。");
                }
                return classfileBuffer;
            }
        });
    }
}
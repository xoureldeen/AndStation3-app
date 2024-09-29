package com.vectras.as3.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TermuxX11 {

    public static void runTermuxX11(String[] args) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                List<String> command = new ArrayList<>(Arrays.asList(
                        "/data/data/com.vectras.as3/files/usr/bin/bash",
                        "-c",
                        "export CLASSPATH=/data/data/com.vectras.as3/files/usr/libexec/termux-x11/loader.apk && "
                                + "unset LD_LIBRARY_PATH LD_PRELOAD && "
                                + "exec /system/bin/app_process -Xnoimage-dex2oat / com.vectras.as3.Loader \"$@\""
                ));

                command.addAll(Arrays.asList(args));

                ProcessBuilder pb = new ProcessBuilder(command);
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;

                System.out.println("Output:");
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                System.out.println("Errors:");
                while ((line = errorReader.readLine()) != null) {
                    System.out.println(line);
                }

                int exitCode = process.waitFor();
                System.out.println("Exited with code: " + exitCode);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        executor.shutdown();
    }

    public static void main(String[] args) {
        runTermuxX11(args);
    }
}

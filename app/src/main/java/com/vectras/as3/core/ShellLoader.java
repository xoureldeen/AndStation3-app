package com.vectras.as3.core;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.vectras.as3.logger.VectrasStatus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShellLoader {
    private static final String TAG = "Vterm";
    private Context context;
    private String user = "root";

    private Process prootProcess;
    private BufferedWriter commandWriter;
    public static String DISPLAY = ":0";
    private File logFile;
    private Handler handler;

    public ShellLoader(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
        // Set up the log file
        logFile = new File("/sdcard/Andstation3/proot.log");
        startProotProcess();
        VectrasStatus.logInfo("ShellLoader Started!");
    }

    private void startProotProcess() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            String filesDir = context.getFilesDir().getAbsolutePath();

            processBuilder
                    .environment()
                    .put("LD_LIBRARY_PATH", "/data/data/com.vectras.as3/files/usr/lib");

            processBuilder.environment().put("HOME", "/root");
            processBuilder.environment().put("USER", user);
            processBuilder
                    .environment()
                    .put(
                            "PATH",
                            "/usr/local/sbin:/usr/local/bin:/bin:/usr/bin:/sbin:/usr/sbin:/usr/games:/usr/local/games");
            processBuilder.environment().put("TERM", "xterm-256color");
            processBuilder.environment().put("TMPDIR", "/tmp");
            processBuilder.environment().put("SHELL", "/bin/bash");

            processBuilder.environment().put("DISPLAY", DISPLAY);
            processBuilder.environment().put("USE_HEAP", "1");
            processBuilder.environment().put("MESA_LOADER_DRIVER_OVERRIDE", "zink");
            processBuilder.environment().put("GALLIUM_DRIVER", "zink");
            processBuilder.environment().put("ZINK_DESCRIPTORS", "lazy");
            processBuilder.environment().put("PULSE_SERVER", "127.0.0.1");

            String[] prootCommand = {
                "/data/data/com.vectras.as3/files/usr/bin/proot",
                "--link2symlink",
                "-0",
                "-r",
                "/data/data/com.vectras.as3/files/fex-rootfs",
                "-b",
                "/dev",
                "-b",
                "/proc",
                "-b",
                "/sys",
                "-b",
                "/data/data/com.vectras.as3/files",
                "-b",
                "/data/data/com.vectras.as3/files/fex-rootfs/root:/dev/shm",
                "-b",
                "/data/data/com.vectras.as3/files/home:/home",
                "-b",
                "/data/data/com.vectras.as3/files/usr/tmp:/tmp",
                "-b",
                "/sdcard",
                "-b",
                "/storage",
                "-w",
                "/root",
                "/bin/FEXInterpreter",
                "/bin/bash",
                "--login"
            };

            processBuilder.command(prootCommand);
            prootProcess = processBuilder.start();
            commandWriter =
                    new BufferedWriter(new OutputStreamWriter(prootProcess.getOutputStream()));

            // Start separate threads for reading output and errors
            new Thread(this::readProcessOutput).start();
            new Thread(this::readProcessError).start();

        } catch (IOException e) {
            Log.e(TAG, "Failed to start prootProcess", e);
            VectrasStatus.logError(e.toString());
        }
    }

    // Read output from process and log/write it
    private void readProcessOutput() {
        try (BufferedReader reader =
                        new BufferedReader(new InputStreamReader(prootProcess.getInputStream()));
                FileWriter logWriter = new FileWriter(logFile, true)) {

            String line;
            Log.d(TAG, "Starting to read process output...");

            while ((line = reader.readLine()) != null) {
                final String outputLine = line;
                Log.d(TAG, "Output line received: " + outputLine); // Add more detailed logging
                handler.post(() -> Log.d(TAG, outputLine));
                logToFile(logWriter, "[OUTPUT] " + outputLine);
                VectrasStatus.logInfo(outputLine);
            }

            Log.d(TAG, "Finished reading process output.");

        } catch (IOException e) {
            Log.e(TAG, "Error reading output from prootProcess", e);
        }
    }

    // Read error output from process and log/write it
    private void readProcessError() {
        try (BufferedReader errorReader =
                        new BufferedReader(new InputStreamReader(prootProcess.getErrorStream()));
                FileWriter logWriter = new FileWriter(logFile, true)) {

            String line;
            Log.d(TAG, "Starting to read process errors...");

            while ((line = errorReader.readLine()) != null) {
                final String errorLine = line;
                Log.d(TAG, "Error line received: " + errorLine);
                handler.post(() -> Log.w(TAG, errorLine));
                logToFile(logWriter, "[ERROR] " + errorLine);
                VectrasStatus.logError(errorLine);
            }

            Log.d(TAG, "Finished reading process errors.");

        } catch (IOException e) {
            Log.e(TAG, "Error reading error stream from prootProcess", e);
        }
    }

    // Helper function to log to file with proper synchronization
    private synchronized void logToFile(FileWriter logWriter, String content) {
        try {
            // Get current date and time
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            // Write the log with timestamp
            logWriter.write("[" + currentTime + "] " + content + "\n");
            logWriter.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error writing to log file", e);
        }
    }

    public void executeShellCommand(
            String userCommand, boolean showResultDialog, Activity dialogActivity) {
        new Thread(
                        () -> {
                            try {
                                if (commandWriter != null) {
                                    commandWriter.write(userCommand);
                                    commandWriter.newLine();
                                    commandWriter.flush();
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "Error writing to prootProcess", e);
                            }
                        })
                .start();
    }
    
    public void killProotProcess() {
        if (prootProcess != null) {
            prootProcess.destroy();
            prootProcess = null;
            VectrasStatus.logInfo("Proot process terminated.");
        } else {
            VectrasStatus.logInfo("No proot process is running.");
        }
    }
}

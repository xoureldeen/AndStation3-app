package com.vectras.as3.core;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;

import com.termux.app.TermuxService;
import com.vectras.as3.VectrasApp;
import com.vectras.as3.x11.CmdEntryPoint;

import dalvik.system.PathClassLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class TermuxX11 {
    private static final String TAG = "as3.TermuxX11";

    public static void main(String[] args) throws ErrnoException {
        String filesDir = VectrasApp.vectrasapp.getFilesDir().getAbsolutePath();
        File xkbConfigRoot = new File(filesDir + "/usr/share/X11/xkb");
        if (!xkbConfigRoot.exists())
            throw new RuntimeException("XKB_CONFIG_ROOT not found: " + xkbConfigRoot);
        Os.setenv("XKB_CONFIG_ROOT", xkbConfigRoot.getAbsolutePath(), true);

        File tmpDir = new File(TermuxService.PREFIX_PATH + "/tmp");
        deleteRecursively(tmpDir);
        tmpDir.mkdirs();
        Os.setenv("TMPDIR", tmpDir.toString(), true);

        try {
            CmdEntryPoint.main(args);
        } catch (AssertionError e) {
            System.err.println(e.getMessage());
        } catch (Throwable e) {
            Log.e(TAG, "Termux:X11 error", e);
            e.printStackTrace(System.err);
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}

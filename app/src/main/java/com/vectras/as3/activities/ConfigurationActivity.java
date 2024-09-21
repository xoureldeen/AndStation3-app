package com.vectras.as3.activities;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.termux.app.TermuxService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.vectras.as3.R;

public class ConfigurationActivity extends AppCompatActivity {

    private static final String CONFIG_FILE_PATH =
            TermuxService.FILES_PATH + "/fex-rootfs/root/.config/rpcs3/config.yml";
    private String[] resolutions = {
        "720x480", "1280x720", "1920x1080"
    };
    private String selectedResolution = "720x480";
    private boolean showFPS = false;

    private TextView resolutionTextView;
    private SwitchMaterial fpsSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            
            getSupportActionBar().setTitle("Configuration");
        }
        
        resolutionTextView = findViewById(R.id.resolutionCard);
        fpsSwitch = findViewById(R.id.fpsSwitch);

        loadConfig();

        CardView resolutionCard = findViewById(R.id.resolutionCV);
        resolutionCard.setOnClickListener(v -> showResolutionDialog());

        fpsSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    showFPS = isChecked;
                    saveConfig();
                    Toast.makeText(
                                    this,
                                    "FPS Display " + (isChecked ? "Enabled" : "Disabled"),
                                    Toast.LENGTH_SHORT)
                            .show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    private void showResolutionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Resolution")
                .setItems(
                        resolutions,
                        (dialog, which) -> {
                            selectedResolution = resolutions[which];
                            resolutionTextView.setText("Resolution: " + selectedResolution);
                            saveConfig();
                        })
                .show();
    }

     private void saveConfig() {
        File configFile = new File(CONFIG_FILE_PATH);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write("Core:\n");
            writer.write("  PPU Decoder: Recompiler (LLVM)\n");
            writer.write("  PPU Threads: 2\n");
            writer.write("  PPU Debug: false\n");
            writer.write("  Save LLVM logs: false\n");
            writer.write("  Use LLVM CPU: \"\"\n");
            writer.write("  Max LLVM Compile Threads: 0\n");
            writer.write("  Enable thread scheduler: false\n");
            writer.write("  Set DAZ and FTZ: false\n");
            writer.write("  SPU Decoder: Recompiler (ASMJIT)\n");
            writer.write("  Lower SPU thread priority: false\n");
            writer.write("  SPU Debug: false\n");
            writer.write("  Preferred SPU Threads: 0\n");
            writer.write("  SPU delay penalty: 3\n");
            writer.write("  SPU loop detection: true\n");
            writer.write("  SPU Block Size: Safe\n");
            writer.write("  Accurate GETLLAR: false\n");
            writer.write("  Accurate PUTLLUC: false\n");
            writer.write("  SPU Verification: true\n");
            writer.write("  SPU Cache: true\n");
            writer.write("  Enable TSX: Enabled\n");
            writer.write("  Accurate xfloat: false\n");
            writer.write("  Approximate xfloat: true\n");
            writer.write("  Debug Console Mode: false\n");
            writer.write("  Lib Loader: Load liblv2.sprx only\n");
            writer.write("  Hook static functions: false\n");
            writer.write("  Load libraries:\n");
            writer.write("    []\n");
            writer.write("  HLE lwmutex: false\n");
            writer.write("VFS:\n");
            writer.write("  $(EmulatorDir): \"\"\n");
            writer.write("  /dev_hdd0/: $(EmulatorDir)dev_hdd0/\n");
            writer.write("  /dev_hdd1/: $(EmulatorDir)dev_hdd1/\n");
            writer.write("  /dev_flash/: \"\"\n");
            writer.write("  /dev_usb000/: $(EmulatorDir)dev_usb000/\n");
            writer.write("  /dev_bdvd/: \"\"\n");
            writer.write("  /app_home/: \"\"\n");
            writer.write("  Enable /host_root/: false\n");
            writer.write("  Initialize Directories: true\n");
            writer.write("  Limit disk cache size: false\n");
            writer.write("  Disk cache maximum size (MB): 5120\n");
            writer.write("Video:\n");
            writer.write("  Renderer: Vulkan\n");
            writer.write("  Resolution: " + selectedResolution + "\n"); // Correctly set resolution
            writer.write("  Aspect ratio: 16:9\n");
            writer.write("  Frame limit: Off\n");
            writer.write("  Write Color Buffers: false\n");
            writer.write("  Write Depth Buffer: false\n");
            writer.write("  Read Color Buffers: false\n");
            writer.write("  Read Depth Buffer: false\n");
            writer.write("  Log shader programs: false\n");
            writer.write("  VSync: false\n");
            writer.write("  Debug output: false\n");
            writer.write("  Debug overlay: false\n");
            writer.write("  Use Legacy OpenGL Buffers: false\n");
            writer.write("  Use GPU texture scaling: false\n");
            writer.write("  Stretch To Display Area: true\n");
            writer.write("  Force High Precision Z buffer: false\n");
            writer.write("  Strict Rendering Mode: false\n");
            writer.write("  Disable ZCull Occlusion Queries: false\n");
            writer.write("  Disable Vertex Cache: false\n");
            writer.write("  Disable FIFO Reordering: false\n");
            writer.write("  Enable Frame Skip: false\n");
            writer.write("  Force CPU Blit: false\n");
            writer.write("  Disable On-Disk Shader Cache: false\n");
            writer.write("  Disable Vulkan Memory Allocator: false\n");
            writer.write("  Use full RGB output range: true\n");
            writer.write("  Disable Asynchronous Shader Compiler: false\n");
            writer.write("  Strict Texture Flushing: false\n");
            writer.write("  Consecutive Frames To Draw: 1\n");
            writer.write("  Consecutive Frames To Skip: 1\n");
            writer.write("  Resolution Scale: 100\n"); // Separate setting
            writer.write("  Anisotropic Filter Override: 0\n");
            writer.write("  Minimum Scalable Dimension: 16\n");
            writer.write("  Driver Recovery Timeout: 1000000\n");
            writer.write("  D3D12:\n");
            writer.write("    Adapter: \"\"\n");
            writer.write("  Vulkan:\n");
            writer.write("    Adapter: Turnip Adreno (TM) 619\n");
            writer.write("    Force FIFO present mode: false\n");
            writer.write("    Force primitive restart flag: false\n");
            writer.write("  Performance Overlay:\n");
            writer.write("    Enabled: false\n");
            writer.write("    Detail level: High\n");
            writer.write("    Metrics update interval (ms): 350\n");
            writer.write("    Font size (px): 10\n");
            writer.write("    Position: Top Left\n");
            writer.write("    Font: n023055ms.ttf\n");
            writer.write("    Horizontal Margin (px): 50\n");
            writer.write("    Vertical Margin (px): 50\n");
            writer.write("    Center Horizontally: false\n");
            writer.write("    Center Vertically: false\n");
            writer.write("    Opacity (%): 70\n");
            writer.write("    Body Color (hex): \"#FFE138FF\"\n");
            writer.write("    Body Background (hex): \"#002339FF\"\n");
            writer.write("    Title Color (hex): \"#F26C24FF\"\n");
            writer.write("    Title Background (hex): \"#00000000\"\n");
            writer.write("  Shader Compilation Hint:\n");
            writer.write("    Position X (px): 20\n");
            writer.write("    Position Y (px): 690\n");
            writer.write("  Shader Loading Dialog:\n");
            writer.write("    Allow custom background: true\n");
            writer.write("    Darkening effect strength: 30\n");
            writer.write("    Blur effect strength: 0\n");
            writer.write("Audio:\n");
            writer.write("  Renderer: PulseAudio\n");
            writer.write("  Dump to file: false\n");
            writer.write("  Convert to 16 bit: false\n");
            writer.write("  Downmix to Stereo: true\n");
            writer.write("  Start Threshold: 1\n");
            writer.write("  Master Volume: 100\n");
            writer.write("  Enable Buffering: true\n");
            writer.write("  Desired Audio Buffer Duration: 100\n");
            writer.write("  Sampling Period Multiplier: 100\n");
            writer.write("  Enable Time Stretching: false\n");
            writer.write("  Time Stretching Threshold: 75\n");
            writer.write("Input/Output:\n");
            writer.write("  Keyboard: \"Null\"\n");
            writer.write("  Mouse: Basic\n");
            writer.write("  Pad: Keyboard\n");
            writer.write("  Camera: \"Null\"\n");
            writer.write("  Camera type: Unknown\n");
            writer.write("  Move: \"Null\"\n");
            writer.write("System:\n");
            writer.write("  Language: English (US)\n");
            writer.write("  Enter button assignment: Enter with cross\n");
            writer.write("Net:\n");
            writer.write("  Connection status: Disconnected\n");
            writer.write("  IP address: 192.168.1.1\n");
            writer.write("Miscellaneous:\n");
            writer.write("  Automatically start games after boot: true\n");
            writer.write("  Exit RPCS3 when process finishes: false\n");
            writer.write("  Start games in fullscreen mode: true\n");
            writer.write("  Show FPS counter in window title: " + showFPS + "\n"); // Update Show FPS
            writer.write("  Show trophy popups: true\n");
            writer.write("  Show shader compilation hint: true\n");
            writer.write("  Use native user interface: true\n");
            writer.write("  Port: 2345\n");
            writer.write("Log: {}\n");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ConfigError", "Failed to save config");
        }
    }

    private void loadConfig() {
        File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists()) {
            saveConfig();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("  Resolution: ")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        selectedResolution = parts[1].trim();
                        Log.d("ConfigDebug", "Loaded resolution: " + selectedResolution); 
                        resolutionTextView.setText("Resolution: " + selectedResolution);
                    }
                } else if (line.startsWith("  Show FPS counter in window title: ")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        showFPS = Boolean.parseBoolean(parts[1].trim());
                        fpsSwitch.setChecked(showFPS);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ConfigError", "Failed to load config");
        }
    }

}

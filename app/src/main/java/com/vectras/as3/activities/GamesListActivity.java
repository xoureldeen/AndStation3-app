package com.vectras.as3.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.os.Handler;
import androidx.appcompat.widget.Toolbar;
import com.vectras.as3.MainService;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.Spanned;
import android.widget.TextView;
import android.text.method.LinkMovementMethod;
import com.vectras.as3.core.ShellLoader;
import com.vectras.as3.core.TermuxX11;
import android.system.ErrnoException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.termux.app.TermuxService;
import com.vectras.as3.R;
import com.vectras.as3.adapters.GamesListAdapter;
import com.vectras.as3.utils.CenterScaleLayoutManager;
import com.vectras.as3.utils.FileUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GamesListActivity extends AppCompatActivity
        implements GamesListAdapter.OnItemClickListener {

    private static final int PICK_ISO_FILE = 1;
    private static final String TAG = "7zExtraction";
    private ProgressDialog progressDialog;
    private Dialog progressDialog2;
    private GamesListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_list);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            
            getSupportActionBar().setTitle("Games");
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabPickIso = findViewById(R.id.fabPickIso);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        CenterScaleLayoutManager layoutManager = new CenterScaleLayoutManager(this, 0.5f);
        recyclerView.setLayoutManager(layoutManager);

        List<File> gamesList = loadGamesList();
        adapter = new GamesListAdapter(this, gamesList, this::showItemOptionsDialog, this);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::refreshGamesList);

        fabPickIso.setOnClickListener(v -> pickIsoFile());
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onItemClick(File file) {
        File ebootFile = new File(file.getAbsolutePath() + "/PS3_GAME/USRDIR/EBOOT.BIN");
        File ebootFile2 = new File(file.getAbsolutePath() + "/USRDIR/EBOOT.BIN");
        if (ebootFile.exists()) {
            showBootingProgressDialog(ebootFile.getAbsolutePath());
        } else if (ebootFile2.exists()) {
            showBootingProgressDialog(ebootFile2.getAbsolutePath());
        }
    }

    private void showBootingProgressDialog(String gameBinPath) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);

        progressDialog2 = new Dialog(this);
        progressDialog2.setContentView(dialogView);
        progressDialog2.setCancelable(false);
        progressDialog2.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        progressDialog2.show();

        ShellLoader shellExec = new ShellLoader(this);
        shellExec.executeShellCommand("XDG_RUNTIME_DIR=${TMPDIR} pulseaudio --start --load=\"module-native-protocol-tcp auth-ip-acl=127.0.0.1 auth-anonymous=1\" --exit-idle-time=-1 &", false, this);
        shellExec.executeShellCommand("rpcs3 --no-gui " + gameBinPath, false, this);

        new Handler()
                .postDelayed(
                        () -> {
                            progressDialog2.dismiss();

                            Intent serviceIntent = new Intent(this, MainService.class);
                            this.startForegroundService(serviceIntent);

                            launchX11();
                        },
                        10000);
        initX11();
    }

    private void pickIsoFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_ISO_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_ISO_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String filePath = FileUtils.getPath(this, uri);
                if (filePath != null && filePath.endsWith(".iso")) {
                    extractIsoFile(filePath);
                } else {
                    new AlertDialog.Builder(this, R.style.MainDialogTheme)
                            .setTitle("Not Supported File")
                            .setMessage("Selected file is not an ISO or invalid path.")
                            .setPositiveButton(
                                    "OK",
                                    (dialog, which) -> {
                                        dialog.dismiss();
                                        refreshGamesList();
                                    })
                            .show();
                }
            }
        }
    }

    private void refreshGamesList() {
        List<File> gamesList = loadGamesList();
        adapter.updateGamesList(gamesList);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showItemOptionsDialog(File file) {
        new AlertDialog.Builder(this, R.style.MainDialogTheme)
                .setTitle("Options")
                .setItems(
                        new String[] {"Delete", "Rename"},
                        (dialog, which) -> {
                            switch (which) {
                                case 0: // Delete
                                    showConfirmDeleteDialog(file);
                                    break;
                                case 1: // Rename
                                    showRenameDialog(file);
                                    break;
                            }
                        })
                .show();
    }

    private void showConfirmDeleteDialog(File file) {
        new AlertDialog.Builder(this, R.style.MainDialogTheme)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {
                            deleteItem(file);
                            refreshGamesList();
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteItem(File file) {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            file.delete();
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    private void showRenameDialog(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MainDialogTheme);
        builder.setTitle("Rename");

        // Inflate the custom view
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_rename, null);
        final EditText editText = view.findViewById(R.id.editTextRename);
        editText.setText(file.getName());

        builder.setView(view);
        builder.setPositiveButton(
                "Rename",
                (dialog, which) -> {
                    String newName = editText.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        File newFile = new File(file.getParent(), newName);
                        if (file.renameTo(newFile)) {
                            refreshGamesList();
                        } else {
                            Toast.makeText(this, "Rename failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void extractIsoFile(String filePath) {
        File isoFile = new File(filePath);
        String fileNameWithoutExt = isoFile.getName().replaceAll("\\.iso$", "");
        File outputDir = getUniqueOutputDir(fileNameWithoutExt);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Extracting...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(
                        () -> {
                            StringBuilder resultBuilder = new StringBuilder();
                            try {
                                String command =
                                        TermuxService.PREFIX_PATH
                                                + "/bin/7z x \""
                                                + filePath
                                                + "\" -o\""
                                                + outputDir.getAbsolutePath()
                                                + "\"";
                                Log.d(TAG, "Executing command: " + command);

                                ProcessBuilder processBuilder =
                                        new ProcessBuilder("/system/bin/sh", "-c", command);
                                processBuilder
                                        .environment()
                                        .put("LD_LIBRARY_PATH", TermuxService.PREFIX_PATH + "/lib");

                                Process process = processBuilder.start();

                                BufferedReader reader =
                                        new BufferedReader(
                                                new InputStreamReader(process.getInputStream()));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    resultBuilder.append(line).append("\n");
                                }

                                BufferedReader errorReader =
                                        new BufferedReader(
                                                new InputStreamReader(process.getErrorStream()));
                                while ((line = errorReader.readLine()) != null) {
                                    resultBuilder.append(line).append("\n");
                                }

                                int exitCode = process.waitFor();

                                runOnUiThread(
                                        () -> {
                                            progressDialog.dismiss();
                                            new AlertDialog.Builder(this, R.style.MainDialogTheme)
                                                    .setTitle("Extraction Result")
                                                    .setMessage(
                                                            resultBuilder.toString()
                                                                    + "\n\nFile Path:\n"
                                                                    + filePath
                                                                    + "\n\nExtraction Command:\n"
                                                                    + command)
                                                    .setPositiveButton(
                                                            "OK",
                                                            (dialog, which) -> {
                                                                dialog.dismiss();
                                                                refreshGamesList();
                                                            })
                                                    .show();
                                        });
                            } catch (Exception e) {
                                resultBuilder
                                        .append("Exception occurred: ")
                                        .append(e.getMessage())
                                        .append("\n");
                                runOnUiThread(
                                        () -> {
                                            progressDialog.dismiss();
                                            new AlertDialog.Builder(this, R.style.MainDialogTheme)
                                                    .setTitle("Extraction Result")
                                                    .setMessage(
                                                            resultBuilder.toString()
                                                                    + "\n\nFile Path:\n"
                                                                    + filePath)
                                                    .setPositiveButton(
                                                            "OK",
                                                            (dialog, which) -> dialog.dismiss())
                                                    .show();
                                        });
                            }
                        })
                .start();
    }

    private void launchX11() {
        Intent intent = new Intent(this, com.vectras.as3.x11.X11Activity.class);
        startActivity(intent);
    }

    private void stopProcess() {
        System.exit(0);
    }

    private void initX11() {
        TermuxX11.main(new String[] {":0"});
    }

    private File getUniqueOutputDir(String baseName) {
        File baseDir =
                new File(getFilesDir(), "fex-rootfs/root/.config/rpcs3/dev_hdd0/game/" + baseName);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
            return baseDir;
        }

        int i = 1;
        File newDir;
        while (true) {
            newDir =
                    new File(
                            getFilesDir(),
                            "fex-rootfs/root/.config/rpcs3/dev_hdd0/game/" + baseName + "-" + i);
            if (!newDir.exists()) {
                newDir.mkdirs();
                return newDir;
            }
            i++;
        }
    }

    private List<File> loadGamesList() {
        List<File> games = new ArrayList<>();
        File gamesDir = new File(getFilesDir(), "fex-rootfs/root/.config/rpcs3/dev_hdd0/game");

        if (gamesDir.exists() && gamesDir.isDirectory()) {
            File[] files = gamesDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        File iconFile = new File(file, "PS3_GAME/ICON0.PNG");
                        File iconFile2 = new File(file, "ICON0.PNG");
                        if (iconFile.exists() || iconFile2.exists()) {
                            games.add(file);
                        }
                    }
                }
            }
        }
        return games;
    }
}

package com.vectras.as3.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Environment;
import android.os.PowerManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.provider.Settings;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.Spanned;
import androidx.appcompat.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.system.ErrnoException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vectras.as3.adapters.GithubUserAdapter;
import com.vectras.as3.core.PulseAudio;
import com.vectras.as3.logger.VectrasStatus;
import com.vectras.as3.adapters.LogsAdapter;
import com.vectras.as3.view.GithubUserView;
import java.io.IOException;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.vectras.as3.VectrasApp;
import androidx.recyclerview.widget.RecyclerView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.termux.app.TermuxActivity;
import com.termux.app.TermuxService;
import com.vectras.as3.MainService;
import com.vectras.as3.core.ShellLoader;
import com.vectras.as3.R;
import com.vectras.as3.core.TermuxX11;
import java.io.File;

import android.app.Dialog;
import android.view.LayoutInflater;

public class MainActivity extends AppCompatActivity {

    public static Activity activity;

    private ExtendedFloatingActionButton fabBoot;
    private Dialog progressDialog;

    public BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("AndStation3");
        }

        fabBoot = findViewById(R.id.fabBoot);

        ShellLoader shellExec = new ShellLoader(this);

        shellExec.executeShellCommand("uname -a", false, activity);

        VectrasStatus.logInfo("Saving proot logs to /sdcard/Andstation3/proot.log");

        //checkAndRequestDisableBatteryOptimization(this);

        initFabPlay();

        fabBoot.setOnClickListener(
                v -> {
                    if (MainService.isStarted) {
                        stopProcess();
                    } else {
                        showBootingProgressDialog();
                    }
                });

        bottomSheetDialog = new BottomSheetDialog(this);
        View view = this.getLayoutInflater().inflate(R.layout.bottomsheetdialog_logger, null);
        bottomSheetDialog.setContentView(view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(VectrasApp.getApp());
        LogsAdapter mLogAdapter = new LogsAdapter(layoutManager, VectrasApp.getApp());
        RecyclerView logList = (RecyclerView) view.findViewById(R.id.recyclerLog);
        logList.setAdapter(mLogAdapter);
        logList.setLayoutManager(layoutManager);
        mLogAdapter.scrollToLastPosition();

        ImageButton loggerButton = findViewById(R.id.loggerButton);

        ImageButton discordButton = findViewById(R.id.discordButton);
        ImageButton telegramButton = findViewById(R.id.telegramButton);
        ImageButton websiteButton = findViewById(R.id.websiteButton);

        loggerButton.setOnClickListener(v -> bottomSheetDialog.show());

        discordButton.setOnClickListener(
                v -> {
                    openUrlInBrowser("https://discord.gg/vEum8EESup");
                });

        telegramButton.setOnClickListener(
                v -> {
                    openUrlInBrowser("https://t.me/AndstationEmulator");
                });

        websiteButton.setOnClickListener(v -> onWebsiteCardClicked(websiteButton));
        
        RecyclerView recyclerView = findViewById(R.id.github_users_recycler_view);

        String[] usernames = {"vectras-team", "xoureldeen", "ahmedbarakat2007", "gamextra4u"};

        GithubUserAdapter adapter = new GithubUserAdapter(this, usernames);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager GitLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(GitLayoutManager);
    }

    private void showAboutDialog() {
        String appVersion;
        try {
            appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            appVersion = "Unknown";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MainDialogTheme);
        builder.setTitle("About the App");

        String message =
                "PS3 Emulator - AndStation3<br>"
                        + "Version: "
                        + appVersion
                        + "<br><br>"
                        + "This emulator is based on RPCS3 and uses FEX rootfs and Proot for execution.<br><br>"
                        + "RPCS3 Version: 0.0.6<br><br>"
                        + "Important: We do not provide a privacy policy, and we do not provide any game files. "
                        + "You must dump your own PS3 games to use with this emulator.<br><br>"
                        + "<a href='https://andstation3.vercel.app'>Visit the official website</a><br><br>"
                        + "Disclaimer: This app is in development and might not support all PS3 games. "
                        + "RPCS3 is an open-source PS3 emulator project.";

        builder.setMessage(Html.fromHtml(message));

        builder.setPositiveButton(
                "OK",
                (dialog, which) -> {
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        TextView messageTextView = dialog.findViewById(android.R.id.message);
        if (messageTextView != null) {
            messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public void initFabPlay() {
        if (MainService.isStarted) {
            fabBoot.setText("STOP RPCS3");
            fabBoot.setIcon(getDrawable(R.drawable.stop_24px));
        } else {
            fabBoot.setText("RUN RPCS3");
            fabBoot.setIcon(getDrawable(R.drawable.play_arrow_24px));
        }
    }

    private void showBootingProgressDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);

        progressDialog = new Dialog(this);
        progressDialog.setContentView(dialogView);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        progressDialog.show();

        File as3Dir = new File(Environment.getExternalStorageDirectory(), "Andstation3");
        if (!as3Dir.exists()) as3Dir.mkdirs();

        initX11();

        ShellLoader shellExec = new ShellLoader(this);

        shellExec.executeShellCommand("rpcs3 >> /sdcard/Andstation3/rpcs3.log", true, this);

        VectrasStatus.logInfo("Saving rpcs3 logs to /sdcard/Andstation3/rpcs3.log");

        new Handler()
                .postDelayed(
                        () -> {
                            progressDialog.dismiss();

                            fabBoot.setText("STOP RPCS3");
                            fabBoot.setIcon(getDrawable(R.drawable.stop_24px));

                            Intent serviceIntent = new Intent(this, MainService.class);
                            this.startForegroundService(serviceIntent);

                            launchX11();

                            new PulseAudio(activity).start();
                        },
                        10000);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (MainService.isStarted) bottomSheetDialog.show();
    }

    public void checkAndRequestDisableBatteryOptimization(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        String packageName = context.getPackageName();

        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            new AlertDialog.Builder(context, R.style.MainDialogTheme)
                    .setTitle("Battery Optimization")
                    .setMessage(
                            "This app may not function optimally if battery optimizations are enabled. Would you like to disable battery optimizations for this app?")
                    .setPositiveButton(
                            "Disable",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent =
                                            new Intent(
                                                    Settings
                                                            .ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                    intent.setData(Uri.parse("package:" + packageName));
                                    context.startActivity(intent);
                                }
                            })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void launchX11() {
        Intent intent = new Intent(activity, com.vectras.as3.x11.X11Activity.class);
        startActivity(intent);
    }

    private void stopProcess() {
        System.exit(0);
    }

    private void initX11() {
        TermuxX11.main(new String[] {":0"});
    }

    public void onGamesCardClicked(View view) {
        startActivity(new Intent(this, GamesListActivity.class));
    }

    public void onTerminalCardClicked(View view) {
        startActivity(new Intent(this, TermuxActivity.class));
    }

    public void onConfigCardClicked(View view) {
        startActivity(new Intent(this, ConfigurationActivity.class));
    }

    public void onAboutCardClicked(View view) {
        showAboutDialog();
    }

    public void onWebsiteCardClicked(View view) {
        String url = "https://andstation3.vercel.app";

        openUrlInBrowser(url);
    }

    public void onRpcs3Clicked(View view) {
        String url = "https://rpcs3.net";

        openUrlInBrowser(url);
    }

    public void openUrlInBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "No browser available to open the link", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}

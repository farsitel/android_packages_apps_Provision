/*
 * Copyright (C) 2008 The Android Open Source Project
 * Copyright (C) 2011 Iranian Supreme Council of ICT, The FarsiTel Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.provision;

import android.app.ActivityManagerNative;
import android.app.Activity;
import android.app.IActivityManager;
import android.app.backup.BackupManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import java.util.Locale;

/**
 * Application that sets the provisioned bit, like SetupWizard does.
 */
public class DefaultActivity extends Activity implements
        View.OnClickListener {

    private final String TAG = "Provision";

    private int page = 0;

    private final static int RESULT_BASE = 0;
    private final static int RESULT_DATE_TIME = RESULT_BASE + 1;
    private final static int RESULT_LANG = RESULT_BASE + 2;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.wizard);

        ((Button) findViewById(R.id.back_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.next_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.welcome_emergency_dial)).setOnClickListener(this);
        ((Button) findViewById(R.id.welcome_change_language)).setOnClickListener(this);
        ((Button) findViewById(R.id.tutorial_start)).setOnClickListener(this);

        initPage();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_DATE_TIME) {
            provisionize();
        } else if (requestCode == RESULT_LANG) {
        }
    }

    private void initPage() {
        Button back_b = (Button) findViewById(R.id.back_button);
        Button next_b = (Button) findViewById(R.id.next_button);
        View page0_box = findViewById(R.id.page0_box);
        View page1_box = findViewById(R.id.page1_box);
        TextView title = (TextView) findViewById(R.id.title_text);
        TextView body = (TextView) findViewById(R.id.body_text);

        if (page == 0) {
            back_b.setVisibility(View.GONE);
            page0_box.setVisibility(View.VISIBLE);
            page1_box.setVisibility(View.GONE);

            title.setText(getString(R.string.start_title));
            body.setText(Html.fromHtml(getString(R.string.page0_message, Build.MODEL)));
            next_b.setText(getString(R.string.begin_button_label));
        } else if (page == 1) {
            back_b.setVisibility(View.VISIBLE);
            page0_box.setVisibility(View.GONE);
            page1_box.setVisibility(View.VISIBLE);

            title.setText(getString(R.string.tutorial_intro_title));
            body.setText(getText(R.string.tutorial_intro_message_generic));
            next_b.setText(getString(R.string.finish_button_label));
        } else if (page == 2) {
            try {
                // Launch DateTimeSettings
                Intent dateTimeSettingsIntent = new Intent();
                dateTimeSettingsIntent.setClassName(
                                "com.android.settings",
                                "com.android.settings.DateTimeSettingsSetupWizard");
                startActivityForResult(dateTimeSettingsIntent, RESULT_DATE_TIME);
            } catch (ActivityNotFoundException anfe) {
                provisionize();
            }
        }
    }

    private void provisionize() {
        // Add a persistent setting to allow other apps to know the device has been provisioned.
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.DEVICE_PROVISIONED, 1);

        // remove this activity from the package manager.
        PackageManager pm = getPackageManager();
        ComponentName name = new ComponentName(this, DefaultActivity.class);
        pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);

        // terminate the activity.
        finish();
    }

    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.back_button) {
            page -= 1;
        } else if (vid == R.id.next_button) {
            page += 1;
        } else if (vid == R.id.welcome_emergency_dial) {
            Intent emergencyIntent = new Intent("com.android.phone.EmergencyDialer.DIAL");
            emergencyIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS |
                                     Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            startActivity(emergencyIntent);
        } else if (vid == R.id.welcome_change_language) {
            Intent dateTimeSettingsIntent = new Intent();
            dateTimeSettingsIntent.setClassName(
                            "com.android.settings",
                            "com.android.settings.LocalePickerInSetupWizard");
            startActivityForResult(dateTimeSettingsIntent, RESULT_LANG);
        } else if (vid == R.id.tutorial_start) {
            Intent tutorialIntent = new Intent("android.intent.action.SYSTEM_TUTORIAL");
            tutorialIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS |
                                     Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            startActivity(tutorialIntent);
        }
        initPage();
    }

}


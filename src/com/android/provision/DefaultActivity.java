/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Bundle;
import android.provider.Settings;

import java.util.Locale;

/**
 * Application that sets the provisioned bit, like SetupWizard does.
 */
public class DefaultActivity extends Activity {

    /**
    * TODO: Document me and move me!
    */
    public static void changeLang(String language, String country) {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();

            Locale locale = new Locale(language, country);
            config.locale = locale;

            // indicate this isn't some passing default - the user wants this remembered
            config.userSetLocale = true;

            am.updateConfiguration(config);
            // Trigger the dirty bit for the Settings Provider.
            BackupManager.dataChanged("com.android.providers.settings");
        } catch (RemoteException e) {
            // Intentionally left blank
        }

    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        changeLang(SystemProperties.get("ro.product.locale.language"),
                   SystemProperties.get("ro.product.locale.region"));

        // Add a persistent setting to allow other apps to know the device has been provisioned.
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.DEVICE_PROVISIONED, 1);

        // remove this activity from the package manager.
        PackageManager pm = getPackageManager();
        ComponentName name = new ComponentName(this, DefaultActivity.class);
        pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);

        // terminate the activity.
        finish();
    }
}


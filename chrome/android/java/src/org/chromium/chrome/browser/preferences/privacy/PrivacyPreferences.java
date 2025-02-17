// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.preferences.privacy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.chromium.chrome.R;
import org.chromium.base.ContextUtils;
import org.chromium.chrome.browser.ChromeFeatureList;
import org.chromium.chrome.browser.contextualsearch.ContextualSearchFieldTrial;
import org.chromium.chrome.browser.help.HelpAndFeedback;
import org.chromium.chrome.browser.physicalweb.PhysicalWeb;
import org.chromium.chrome.browser.precache.PrecacheLauncher;
import org.chromium.chrome.browser.preferences.ChromeBaseCheckBoxPreference;
import org.chromium.chrome.browser.preferences.ManagedPreferenceDelegate;
import org.chromium.chrome.browser.preferences.PrefServiceBridge;
import org.chromium.chrome.browser.preferences.PreferenceUtils;
import org.chromium.chrome.browser.profiles.Profile;
import org.chromium.chrome.browser.MixPanelWorker;

/**
 * Fragment to keep track of the all the privacy related preferences.
 */
public class PrivacyPreferences extends PreferenceFragment
        implements OnPreferenceChangeListener {
    private static final String PREF_NAVIGATION_ERROR = "navigation_error";
    private static final String PREF_SEARCH_SUGGESTIONS = "search_suggestions";
    /*private static final String PREF_SAFE_BROWSING_EXTENDED_REPORTING =
            "safe_browsing_extended_reporting";
    private static final String PREF_SAFE_BROWSING_SCOUT_REPORTING =
            "safe_browsing_scout_reporting";*/
    private static final String PREF_SAFE_BROWSING = "safe_browsing";
    private static final String PREF_FINGERPRINTING_PROTECTION = "fingerprinting_protection";
    private static final String PREF_HTTPSE = "httpse";
    private static final String PREF_TRACKING_PROTECTION = "tracking_protection";
    private static final String PREF_AD_BLOCK = "ad_block";
    private static final String PREF_AD_BLOCK_REGIONAL = "ad_block_regional";
    private static final String PREF_CONTEXTUAL_SEARCH = "contextual_search";
    private static final String PREF_NETWORK_PREDICTIONS = "network_predictions";
    private static final String PREF_CLEAR_BROWSING_DATA = "clear_browsing_data";
    private static final String PREF_SEND_METRICS = "send_metrics";
    //private static final String PREF_DO_NOT_TRACK = "do_not_track";
    //private static final String PREF_USAGE_AND_CRASH_REPORTING = "usage_and_crash_reports";
    //private static final String PREF_PHYSICAL_WEB = "physical_web";

    private ManagedPreferenceDelegate mManagedPreferenceDelegate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrivacyPreferencesManager privacyPrefManager = PrivacyPreferencesManager.getInstance();
        privacyPrefManager.migrateNetworkPredictionPreferences();
        PreferenceUtils.addPreferencesFromResource(this, R.xml.privacy_preferences);
        getActivity().setTitle(R.string.prefs_privacy);
        setHasOptionsMenu(true);
        PrefServiceBridge prefServiceBridge = PrefServiceBridge.getInstance();

        mManagedPreferenceDelegate = createManagedPreferenceDelegate();

        ChromeBaseCheckBoxPreference networkPredictionPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_NETWORK_PREDICTIONS);
        networkPredictionPref.setChecked(prefServiceBridge.getNetworkPredictionEnabled());
        networkPredictionPref.setOnPreferenceChangeListener(this);
        networkPredictionPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

        ChromeBaseCheckBoxPreference navigationErrorPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_NAVIGATION_ERROR);
        navigationErrorPref.setOnPreferenceChangeListener(this);
        navigationErrorPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

        ChromeBaseCheckBoxPreference searchSuggestionsPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_SEARCH_SUGGESTIONS);
        searchSuggestionsPref.setOnPreferenceChangeListener(this);
        searchSuggestionsPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);
        if (ChromeFeatureList.isEnabled(ChromeFeatureList.CONTENT_SUGGESTIONS_SETTINGS)) {
            searchSuggestionsPref.setTitle(R.string.search_site_suggestions_title);
            searchSuggestionsPref.setSummary(R.string.search_site_suggestions_summary);
        }

        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (!ContextualSearchFieldTrial.isEnabled()) {
            preferenceScreen.removePreference(findPreference(PREF_CONTEXTUAL_SEARCH));
        }

        // Listen to changes to both Extended Reporting prefs.
        /*ChromeBaseCheckBoxPreference legacyExtendedReportingPref =
                (ChromeBaseCheckBoxPreference) findPreference(
                    PREF_SAFE_BROWSING_EXTENDED_REPORTING);
        legacyExtendedReportingPref.setOnPreferenceChangeListener(this);
        legacyExtendedReportingPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);
        ChromeBaseCheckBoxPreference scoutReportingPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_SAFE_BROWSING_SCOUT_REPORTING);
        scoutReportingPref.setOnPreferenceChangeListener(this);
        scoutReportingPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);
        // Remove the extended reporting preference that is NOT active.
        String extended_reporting_pref_to_remove =
                prefServiceBridge.isSafeBrowsingScoutReportingActive()
                    ? PREF_SAFE_BROWSING_EXTENDED_REPORTING : PREF_SAFE_BROWSING_SCOUT_REPORTING;
        preferenceScreen.removePreference(findPreference(extended_reporting_pref_to_remove));*/

        ChromeBaseCheckBoxPreference safeBrowsingPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_SAFE_BROWSING);
        safeBrowsingPref.setOnPreferenceChangeListener(this);
        safeBrowsingPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

        ChromeBaseCheckBoxPreference fingerprintingProtectionPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_FINGERPRINTING_PROTECTION);
        fingerprintingProtectionPref.setOnPreferenceChangeListener(this);
        fingerprintingProtectionPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

        ChromeBaseCheckBoxPreference httpsePref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_HTTPSE);
        httpsePref.setOnPreferenceChangeListener(this);
        httpsePref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

        ChromeBaseCheckBoxPreference trackingProtectionPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_TRACKING_PROTECTION);
        trackingProtectionPref.setOnPreferenceChangeListener(this);
        trackingProtectionPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

        ChromeBaseCheckBoxPreference adBlockPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_AD_BLOCK);
        adBlockPref.setOnPreferenceChangeListener(this);
        adBlockPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

        ChromeBaseCheckBoxPreference adBlockRegionalPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_AD_BLOCK_REGIONAL);
        adBlockRegionalPref.setOnPreferenceChangeListener(this);
        adBlockRegionalPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

        ChromeBaseCheckBoxPreference sendMetricsPref =
                (ChromeBaseCheckBoxPreference) findPreference(PREF_SEND_METRICS);
        sendMetricsPref.setOnPreferenceChangeListener(this);
        sendMetricsPref.setManagedPreferenceDelegate(mManagedPreferenceDelegate);

        /*if (!PhysicalWeb.featureIsEnabled()) {
            preferenceScreen.removePreference(findPreference(PREF_PHYSICAL_WEB));
        }*/

        if (ClearBrowsingDataTabsFragment.isFeatureEnabled()) {
            findPreference(PREF_CLEAR_BROWSING_DATA)
                    .setFragment(ClearBrowsingDataTabsFragment.class.getName());
        }

        updateSummaries();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (PREF_SEARCH_SUGGESTIONS.equals(key)) {
            PrefServiceBridge.getInstance().setSearchSuggestEnabled((boolean) newValue);
        } else if (PREF_SAFE_BROWSING.equals(key)) {
            PrefServiceBridge.getInstance().setSafeBrowsingEnabled((boolean) newValue);
        } else if (PREF_FINGERPRINTING_PROTECTION.equals(key)) {
            PrefServiceBridge.getInstance().setFingerprintingProtectionEnabled((boolean) newValue);
            MixPanelWorker.SendEvent("Fingerprinting Protection Option Changed", "Fingerprinting Protection", newValue);
        } else if (PREF_HTTPSE.equals(key)) {
            PrefServiceBridge.getInstance().setHTTPSEEnabled((boolean) newValue);
            MixPanelWorker.SendEvent("HTTPS Everywhere Option Changed", "HTTPS Everywhere", newValue);
        } else if (PREF_TRACKING_PROTECTION.equals(key)) {
            PrefServiceBridge.getInstance().setTrackingProtectionEnabled((boolean) newValue);
            MixPanelWorker.SendEvent("Tracking Protection Mode Option Changed", "Tracking Protection Mode", newValue);
        } else if (PREF_AD_BLOCK.equals(key)) {
            PrefServiceBridge.getInstance().setAdBlockEnabled((boolean) newValue);
            MixPanelWorker.SendEvent("Ad Block Option Changed", "Ad Block", newValue);
        } else if (PREF_AD_BLOCK_REGIONAL.equals(key)) {
            PrefServiceBridge.getInstance().setAdBlockRegionalEnabled((boolean) newValue);
            MixPanelWorker.SendEvent("Regional Ad Block Option Changed", "Regional Ad Block", newValue);
        } /*else if (PREF_SAFE_BROWSING_EXTENDED_REPORTING.equals(key)
                   || PREF_SAFE_BROWSING_SCOUT_REPORTING.equals(key)) {
            PrefServiceBridge.getInstance().setSafeBrowsingExtendedReportingEnabled(
                    (boolean) newValue);
        }*/ else if (PREF_NETWORK_PREDICTIONS.equals(key)) {
            PrefServiceBridge.getInstance().setNetworkPredictionEnabled((boolean) newValue);
            PrecacheLauncher.updatePrecachingEnabled(getActivity());
        } else if (PREF_NAVIGATION_ERROR.equals(key)) {
            PrefServiceBridge.getInstance().setResolveNavigationErrorEnabled((boolean) newValue);
        } else if (PREF_SEND_METRICS.equals(key)) {
            SharedPreferences.Editor sharedPreferencesEditor = ContextUtils.getAppSharedPreferences().edit();
            sharedPreferencesEditor.putBoolean(PREF_SEND_METRICS, (boolean)newValue);
            sharedPreferencesEditor.apply();
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSummaries();
    }

    /**
     * Updates the summaries for several preferences.
     */
    public void updateSummaries() {
        PrefServiceBridge prefServiceBridge = PrefServiceBridge.getInstance();

        PrivacyPreferencesManager privacyPrefManager = PrivacyPreferencesManager.getInstance();

        CharSequence textOn = getActivity().getResources().getText(R.string.text_on);
        CharSequence textOff = getActivity().getResources().getText(R.string.text_off);

        CheckBoxPreference navigationErrorPref = (CheckBoxPreference) findPreference(
                PREF_NAVIGATION_ERROR);
        if (navigationErrorPref != null) {
            navigationErrorPref.setChecked(
                    prefServiceBridge.isResolveNavigationErrorEnabled());
        }

        CheckBoxPreference searchSuggestionsPref = (CheckBoxPreference) findPreference(
                PREF_SEARCH_SUGGESTIONS);
        if (searchSuggestionsPref != null) {
            searchSuggestionsPref.setChecked(prefServiceBridge.isSearchSuggestEnabled());
        }

        /*String extended_reporting_pref = prefServiceBridge.isSafeBrowsingScoutReportingActive()
                ? PREF_SAFE_BROWSING_SCOUT_REPORTING : PREF_SAFE_BROWSING_EXTENDED_REPORTING;
        CheckBoxPreference extendedReportingPref =
                (CheckBoxPreference) findPreference(extended_reporting_pref);
        if (extendedReportingPref != null) {
            extendedReportingPref.setChecked(
                    prefServiceBridge.isSafeBrowsingExtendedReportingEnabled());
        }*/

        CheckBoxPreference safeBrowsingPref =
                (CheckBoxPreference) findPreference(PREF_SAFE_BROWSING);
        if (safeBrowsingPref != null) {
            safeBrowsingPref.setChecked(prefServiceBridge.isSafeBrowsingEnabled());
        }

        //Preference doNotTrackPref = findPreference(PREF_DO_NOT_TRACK);
        //if (doNotTrackPref != null) {
        //    doNotTrackPref.setSummary(prefServiceBridge.isDoNotTrackEnabled() ? textOn : textOff);
        //}

        Preference contextualPref = findPreference(PREF_CONTEXTUAL_SEARCH);
        if (contextualPref != null) {
            boolean isContextualSearchEnabled = !prefServiceBridge.isContextualSearchDisabled();
            contextualPref.setSummary(isContextualSearchEnabled ? textOn : textOff);
        }

        Preference regionalAdBlockPref = findPreference(PREF_AD_BLOCK_REGIONAL);
        if (null == regionalAdBlockPref) {
            return;
        }
        if (PrivacyPreferencesManager.getInstance().isRegionalAdBlockEnabled()) {
            regionalAdBlockPref.setSummary(getActivity().getResources().getText(R.string.ad_block_regional_summary));
            regionalAdBlockPref.setEnabled(true);
        } else {
            regionalAdBlockPref.setSummary(getActivity().getResources().getText(R.string.ad_block_regional_summary_no_list));
            regionalAdBlockPref.setEnabled(false);
        }

        /*Preference physicalWebPref = findPreference(PREF_PHYSICAL_WEB);
        if (physicalWebPref != null) {
            physicalWebPref.setSummary(privacyPrefManager.isPhysicalWebEnabled()
                    ? textOn : textOff);
        }*/

        /*Preference usageAndCrashPref = findPreference(PREF_USAGE_AND_CRASH_REPORTING);
        if (usageAndCrashPref != null) {
            usageAndCrashPref.setSummary(
                    privacyPrefManager.isUsageAndCrashReportingPermittedByUser() ? textOn
                                                                                 : textOff);
        }*/
    }

    private ManagedPreferenceDelegate createManagedPreferenceDelegate() {
        return new ManagedPreferenceDelegate() {
            @Override
            public boolean isPreferenceControlledByPolicy(Preference preference) {
                String key = preference.getKey();
                PrefServiceBridge prefs = PrefServiceBridge.getInstance();
                if (PREF_NAVIGATION_ERROR.equals(key)) {
                    return prefs.isResolveNavigationErrorManaged();
                }
                if (PREF_SEARCH_SUGGESTIONS.equals(key)) {
                    return prefs.isSearchSuggestManaged();
                }
                /*if (PREF_SAFE_BROWSING_EXTENDED_REPORTING.equals(key)
                        || PREF_SAFE_BROWSING_SCOUT_REPORTING.equals(key)) {
                    return prefs.isSafeBrowsingExtendedReportingManaged();
                }*/
                if (PREF_SAFE_BROWSING.equals(key)) {
                    return prefs.isSafeBrowsingManaged();
                }
                if (PREF_NETWORK_PREDICTIONS.equals(key)) {
                    return prefs.isNetworkPredictionManaged();
                }
                return false;
            }
        };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        /*MenuItem help = menu.add(
                Menu.NONE, R.id.menu_id_targeted_help, Menu.NONE, R.string.menu_help);
        help.setIcon(R.drawable.ic_help_and_feedback);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_id_targeted_help) {
            HelpAndFeedback.getInstance(getActivity())
                    .show(getActivity(), getString(R.string.help_context_privacy),
                            Profile.getLastUsedProfile(), null);
            return true;
        }
        return false;
    }
}

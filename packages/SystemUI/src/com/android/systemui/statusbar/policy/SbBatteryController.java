/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.systemui.statusbar.policy;

import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter.BluetoothStateChangeCallback;
import android.content.BroadcastReceiver;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.CharacterStyle;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.BatteryManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;

public class SbBatteryController extends LinearLayout {
    private static final String TAG = "StatusBar.BatteryController";

    private Context mContext;
    private ArrayList<ImageView> mIconViews = new ArrayList<ImageView>();
    private ArrayList<TextView> mLabelViews = new ArrayList<TextView>();

    private ArrayList<BatteryStateChangeCallback> mChangeCallbacks =
            new ArrayList<BatteryStateChangeCallback>();

    public interface BatteryStateChangeCallback {
        public void onBatteryLevelChanged(int level, boolean pluggedIn);
    }

    private ImageView mBatteryIcon;
    private TextView mBatteryText;
    private TextView mBatteryCenterText;
    private ViewGroup mBatteryGroup;
    private TextView mBatteryTextOnly;
    private TextView mBatteryTextOnly_Low;
    private TextView mBatteryTextOnly_Plugged;

    private static int mBatteryStyle;

    private int mLevel = -1;
    private boolean mPlugged = false;

    public static final int STYLE_ICON_ONLY = 0;
    public static final int STYLE_TEXT_ONLY = 1;
    public static final int STYLE_ICON_TEXT = 2;
    public static final int STYLE_ICON_CENTERED_TEXT = 3;
    public static final int STYLE_ICON_CIRCLE = 4;
    public static final int BATTERY_STYLE_CIRCLE         = 5;
    public static final int BATTERY_STYLE_CIRCLE_PERCENT = 6;
    public static final int BATTERY_STYLE_DOTTED_CIRCLE_PERCENT = 7;
    public static final int STYLE_ICON_AXIOM = 8;
    public static final int STYLE_ICON_CM = 9;
    public static final int STYLE_ICON_BLUE = 10;
    public static final int STYLE_ICON_CYBORG = 11;
    public static final int STYLE_ICON_FOURBAR = 12;
    public static final int STYLE_ICON_GAUGE = 13;
    public static final int STYLE_ICON_GAUGEMOD = 14;
    public static final int STYLE_ICON_GUMMY = 15;
    public static final int STYLE_ICON_HONEY = 16;
    public static final int STYLE_ICON_LUCID = 17;
    public static final int STYLE_ICON_NEWBORN3 = 18;
    public static final int STYLE_ICON_NUMBERS = 19;
    public static final int STYLE_ICON_OLDAOKP = 20;
    public static final int STYLE_ICON_RAINBOW = 21;
    public static final int STYLE_ICON_SENSE = 22;
    public static final int STYLE_ICON_TRIBUTE = 23;
    public static final int STYLE_ICON_WHITECIRCLE = 24;
    public static final int STYLE_ICON_SQUARE = 25;
    public static final int STYLE_ICON_ALTCIRCLE = 26;
    public static final int STYLE_ICON_BRICK = 27;
    public static final int STYLE_ICON_PLANET = 28;
    public static final int STYLE_ICON_RACING = 29;
    public static final int STYLE_ICON_SLIDER = 30;
    public static final int STYLE_HIDE = 31;

    public SbBatteryController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
        mBatteryGroup = (ViewGroup) findViewById(R.id.battery_combo);
        mBatteryIcon = (ImageView) findViewById(R.id.battery);
        mBatteryText = (TextView) findViewById(R.id.battery_text);
        mBatteryCenterText = (TextView) findViewById(R.id.battery_text_center);
        mBatteryTextOnly = (TextView) findViewById(R.id.battery_text_only);
        mBatteryTextOnly_Low = (TextView) findViewById(R.id.battery_text_only_low);
        mBatteryTextOnly_Plugged = (TextView) findViewById(R.id.battery_text_only_plugged);
        addIconView(mBatteryIcon);

        SettingsObserver settingsObserver = new SettingsObserver(new Handler());
        settingsObserver.observe();
        updateSettings(); // to initialize values
    }

    private void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mBatteryBroadcastReceiver, filter);
    }

    public void addIconView(ImageView v) {
        mIconViews.add(v);
    }

    public void addLabelView(TextView v) {
        mLabelViews.add(v);
    }

    public void addStateChangedCallback(BatteryStateChangeCallback cb) {
        mChangeCallbacks.add(cb);
    }

    private BroadcastReceiver mBatteryBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                final int level = intent.getIntExtra(
                        BatteryManager.EXTRA_LEVEL, 0);
                final boolean plugged = intent.getIntExtra(
                        BatteryManager.EXTRA_PLUGGED, 0) != 0;
                setBatteryIcon(level, plugged);
            }
        }
    };

    private void setBatteryIcon(int level, boolean plugged) {
        mLevel = level;
        mPlugged = plugged;
        ContentResolver cr = mContext.getContentResolver();
        mBatteryStyle = Settings.System.getInt(cr,
                Settings.System.STATUSBAR_BATTERY_ICON, 29);
        int icon;
        switch (mBatteryStyle) {
            case STYLE_ICON_CIRCLE:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_circle
                 : R.drawable.stat_sys_battery_circle;
                 break;
            case STYLE_ICON_AXIOM:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_axiom
                 : R.drawable.stat_sys_battery_axiom;
                 break;
            case STYLE_ICON_CM:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_min
                 : R.drawable.stat_sys_battery_min;
                 break;
            case STYLE_ICON_BLUE:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_blue
                 : R.drawable.stat_sys_battery_blue;
                 break;
            case STYLE_ICON_CYBORG:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_cyborg
                 : R.drawable.stat_sys_battery_cyborg;
                 break;
            case STYLE_ICON_FOURBAR:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_fourbar
                 : R.drawable.stat_sys_battery_fourbar;
                 break;
            case STYLE_ICON_GAUGE:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_gauge
                 : R.drawable.stat_sys_battery_gauge;
                 break;
            case STYLE_ICON_GAUGEMOD:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_gaugemod
                 : R.drawable.stat_sys_battery_gaugemod;
                 break;
            case STYLE_ICON_GUMMY:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_gummy
                 : R.drawable.stat_sys_battery_gummy;
                 break;
            case STYLE_ICON_HONEY:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_honey
                 : R.drawable.stat_sys_battery_honey;
                 break;
            case STYLE_ICON_LUCID:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_lucid
                 : R.drawable.stat_sys_battery_lucid;
                 break;
            case STYLE_ICON_NEWBORN3:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_newborn3
                 : R.drawable.stat_sys_battery_newborn3;
                 break;
            case STYLE_ICON_NUMBERS:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_numbers
                 : R.drawable.stat_sys_battery_numbers;
                 break;
            case STYLE_ICON_OLDAOKP:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_oldaokp
                 : R.drawable.stat_sys_battery_oldaokp;
                 break;
            case STYLE_ICON_RAINBOW:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_rainbow
                 : R.drawable.stat_sys_battery_rainbow;
                 break;
            case STYLE_ICON_SENSE:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_sense
                 : R.drawable.stat_sys_battery_sense;
                 break;
            case STYLE_ICON_TRIBUTE:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_tribute
                 : R.drawable.stat_sys_battery_tribute;
                 break;
            case STYLE_ICON_WHITECIRCLE:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_whitecircle
                 : R.drawable.stat_sys_battery_whitecircle;
                 break;
            case STYLE_ICON_SQUARE:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_square
                 : R.drawable.stat_sys_battery_square;
                 break;
            case STYLE_ICON_ALTCIRCLE:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_altcircle
                 : R.drawable.stat_sys_battery_altcircle;
                 break;
            case STYLE_ICON_BRICK:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_brick
                 : R.drawable.stat_sys_battery_brick;
                 break;
            case STYLE_ICON_PLANET:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_planet
                 : R.drawable.stat_sys_battery_planet;
                 break;
            case STYLE_ICON_RACING:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_racing
                 : R.drawable.stat_sys_battery_racing;
                 break;
            case STYLE_ICON_SLIDER:
                 icon = plugged ? R.drawable.stat_sys_battery_charge_slider
                 : R.drawable.stat_sys_battery_slider;
                 break;
            default:
                 icon = plugged ? R.drawable.stat_sys_battery_charge
                 : R.drawable.stat_sys_battery;
                 break;
        }
        int N = mIconViews.size();
        for (int i = 0; i < N; i++) {
            ImageView v = mIconViews.get(i);
            v.setImageResource(icon);
            v.setImageLevel(level);
            v.setContentDescription(mContext.getString(
                    R.string.accessibility_battery_level, level));
        }
        N = mLabelViews.size();
        for (int i = 0; i < N; i++) {
            TextView v = mLabelViews.get(i);
            v.setText(mContext.getString(
                    R.string.status_bar_settings_battery_meter_format, level));
        }

        // do my stuff here
        if (mBatteryGroup != null) {
            mBatteryText.setText(Integer.toString(level));
            mBatteryCenterText.setText(Integer.toString(level));
            SpannableStringBuilder formatted = new SpannableStringBuilder(
                    Integer.toString(level) + "%");
            CharacterStyle style = new RelativeSizeSpan(0.7f); // beautiful
                                                               // formatting
            if (level < 10) { // level < 10, 2nd char is %
                formatted.setSpan(style, 1, 2,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            } else if (level < 100) { // level 10-99, 3rd char is %
                formatted.setSpan(style, 2, 3,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            } else { // level 100, 4th char is %
                formatted.setSpan(style, 3, 4,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
            mBatteryTextOnly.setText(formatted);
            mBatteryTextOnly_Low.setText(formatted);
            mBatteryTextOnly_Plugged.setText(formatted);
            if (mBatteryStyle == STYLE_TEXT_ONLY) {
                if (plugged) {
                    mBatteryTextOnly.setVisibility(View.GONE);
                    mBatteryTextOnly_Plugged.setVisibility(View.VISIBLE);
                    mBatteryTextOnly_Low.setVisibility(View.GONE);
                } else if (level < 16) {
                    mBatteryTextOnly.setVisibility(View.GONE);
                    mBatteryTextOnly_Plugged.setVisibility(View.GONE);
                    mBatteryTextOnly_Low.setVisibility(View.VISIBLE);
                } else {
                    mBatteryTextOnly.setVisibility(View.VISIBLE);
                    mBatteryTextOnly_Plugged.setVisibility(View.GONE);
                    mBatteryTextOnly_Low.setVisibility(View.GONE);
                }
            } else {
                mBatteryTextOnly.setVisibility(View.GONE);
                mBatteryTextOnly_Plugged.setVisibility(View.GONE);
                mBatteryTextOnly_Low.setVisibility(View.GONE);
            }

        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System
                    .getUriFor(Settings.System.STATUSBAR_BATTERY_ICON), false,
                    this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    private void updateSettings() {
        // Slog.i(TAG, "updated settings values");
        ContentResolver cr = mContext.getContentResolver();
        mBatteryStyle = Settings.System.getInt(cr,
                Settings.System.STATUSBAR_BATTERY_ICON, 29);

        switch (mBatteryStyle) {
            case STYLE_ICON_ONLY:
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_TEXT_ONLY:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.GONE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_TEXT:
                mBatteryText.setVisibility(View.VISIBLE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_CENTERED_TEXT:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.VISIBLE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_HIDE:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.GONE);
                setVisibility(View.GONE);
                break;
            case STYLE_ICON_CIRCLE:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
	    case BATTERY_STYLE_CIRCLE:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.GONE);
                setVisibility(View.VISIBLE);
                break;
	    case BATTERY_STYLE_CIRCLE_PERCENT:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.GONE);
                setVisibility(View.VISIBLE);
                break;
	    case BATTERY_STYLE_DOTTED_CIRCLE_PERCENT:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.GONE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_AXIOM:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_CM:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_BLUE:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_CYBORG:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_FOURBAR:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_GAUGE:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_GAUGEMOD:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_GUMMY:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_HONEY:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_LUCID:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_NEWBORN3:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_NUMBERS:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_OLDAOKP:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_RAINBOW:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_SENSE:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_TRIBUTE:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_WHITECIRCLE:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_SQUARE:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_ALTCIRCLE:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_BRICK:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_PLANET:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_RACING:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            case STYLE_ICON_SLIDER:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
            default:
                mBatteryText.setVisibility(View.GONE);
                mBatteryCenterText.setVisibility(View.GONE);
                mBatteryIcon.setVisibility(View.VISIBLE);
                setVisibility(View.VISIBLE);
                break;
        }

        setBatteryIcon(mLevel, mPlugged);

    }
}

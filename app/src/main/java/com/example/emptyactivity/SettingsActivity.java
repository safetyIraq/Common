package com.example.emptyactivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settingsToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // ربط عناصر الإعدادات
        setupSettingItem(R.id.setting_profile, "الملف الشخصي", "تعديل معلوماتك الشخصية");
        setupSettingItem(R.id.setting_notifications, "الإشعارات", "تخصيص الإشعارات");
        setupSettingItem(R.id.setting_privacy, "الخصوصية", "إعدادات الخصوصية");
        setupSettingItem(R.id.setting_language, "اللغة", "تغيير لغة التطبيق");
        setupSettingItem(R.id.setting_theme, "المظهر", "الوضع الفاتح/الداكن");
        setupSettingItem(R.id.setting_about, "حول", "معلومات التطبيق");
    }

    private void setupSettingItem(int id, String title, String summary) {
        View view = findViewById(id);
        if (view != null) {
            TextView titleView = view.findViewById(R.id.settingTitle);
            TextView summaryView = view.findViewById(R.id.settingSummary);

            if (titleView != null) titleView.setText(title);
            if (summaryView != null) {
                summaryView.setText(summary);
                summaryView.setVisibility(View.VISIBLE);
            }

            view.setOnClickListener(v -> 
                Toast.makeText(this, "تم فتح " + title, Toast.LENGTH_SHORT).show()
            );
        }
    }
}

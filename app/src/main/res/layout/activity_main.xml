<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/black"
    android:layoutDirection="rtl">

    <!-- شاشة المصادقة (تسجيل الدخول / إنشاء حساب) -->
    <RelativeLayout
        android:id="@+id/authView"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <!-- صورة الخلفية -->
        <ImageView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scaleType="centerCrop"
            android:src="@drawable/bg_login"
            app:tint="#4D000000"
            app:tintMode="src_over" />

        <!-- مؤشر التحميل -->
        <ProgressBar
            android:id="@+id/loadingView"
            android:layout_width="60dp"
            android:layout_height="60dp"
            android:layout_centerInParent="true"
            android:elevation="10dp"
            android:indeterminateTint="@color/white"
            android:visibility="gone" />

        <!-- محتوى النموذج القابل للتمرير -->
        <ScrollView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:fillViewport="true">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="30dp"
                android:gravity="center">

                <!-- العنوان الرئيسي -->
                <TextView
                    android:id="@+id/tvTitle"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="تسجيل الدخول"
                    android:textSize="36sp"
                    android:textStyle="bold"
                    android:textColor="@color/white"
                    android:layout_marginTop="50dp"
                    android:layout_marginBottom="30dp"
                    android:shadowColor="#000000"
                    android:shadowDx="3"
                    android:shadowDy="3"
                    android:shadowRadius="5" />

                <!-- حقل اسم المستخدم (يظهر فقط في وضع إنشاء الحساب) -->
                <com.google.android.material.textfield.TextInputLayout
                    android:id="@+id/layoutUser"
                    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="15dp"
                    android:visibility="gone"
                    app:boxBackgroundColor="@color/glass_dark_box"
                    app:boxStrokeColor="@color/glass_stroke"
                    app:boxCornerRadiusTopStart="20dp"
                    app:boxCornerRadiusTopEnd="20dp"
                    app:boxCornerRadiusBottomStart="20dp"
                    app:boxCornerRadiusBottomEnd="20dp"
                    app:startIconDrawable="@android:drawable/ic_menu_myplaces"
                    app:startIconTint="@color/white"
                    app:hintTextColor="@color/white">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/regUser"
                        android:layout_width="match_parent"
                        android:layout_height="60dp"
                        android:hint="اسم المستخدم"
                        android:textColorHint="@color/text_hint"
                        android:textSize="16sp"
                        android:textColor="@color/white" />
                </com.google.android.material.textfield.TextInputLayout>

                <!-- حقل البريد الإلكتروني -->
                <com.google.android.material.textfield.TextInputLayout
                    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="15dp"
                    app:boxBackgroundColor="@color/glass_dark_box"
                    app:boxStrokeColor="@color/glass_stroke"
                    app:boxCornerRadiusTopStart="20dp"
                    app:boxCornerRadiusTopEnd="20dp"
                    app:boxCornerRadiusBottomStart="20dp"
                    app:boxCornerRadiusBottomEnd="20dp"
                    app:startIconDrawable="@android:drawable/ic_dialog_email"
                    app:startIconTint="@color/white"
                    app:hintTextColor="@color/white">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/regEmail"
                        android:layout_width="match_parent"
                        android:layout_height="60dp"
                        android:hint="البريد الإلكتروني"
                        android:inputType="textEmailAddress"
                        android:textColorHint="@color/text_hint"
                        android:textSize="16sp"
                        android:textColor="@color/white" />
                </com.google.android.material.textfield.TextInputLayout>

                <!-- حقل كلمة المرور -->
                <com.google.android.material.textfield.TextInputLayout
                    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="10dp"
                    app:boxBackgroundColor="@color/glass_dark_box"
                    app:boxStrokeColor="@color/glass_stroke"
                    app:boxCornerRadiusTopStart="20dp"
                    app:boxCornerRadiusTopEnd="20dp"
                    app:boxCornerRadiusBottomStart="20dp"
                    app:boxCornerRadiusBottomEnd="20dp"
                    app:startIconDrawable="@android:drawable/ic_lock_idle_lock"
                    app:startIconTint="@color/white"
                    app:passwordToggleEnabled="true"
                    app:passwordToggleTint="@color/white"
                    app:hintTextColor="@color/white">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/regPass"
                        android:layout_width="match_parent"
                        android:layout_height="60dp"
                        android:hint="كلمة المرور"
                        android:inputType="textPassword"
                        android:textColorHint="@color/text_hint"
                        android:textSize="16sp"
                        android:textColor="@color/white" />
                </com.google.android.material.textfield.TextInputLayout>

                <!-- زر الإجراء الرئيسي -->
                <com.google.android.material.button.MaterialButton
                    android:id="@+id/mainActionBtn"
                    android:layout_width="match_parent"
                    android:layout_height="65dp"
                    android:text="تسجيل الدخول"
                    android:textSize="20sp"
                    android:textStyle="bold"
                    android:textColor="@color/white"
                    android:backgroundTint="@color/blue_primary"
                    app:cornerRadius="20dp"
                    android:layout_marginTop="10dp"
                    android:layout_marginBottom="25dp" />

                <!-- نص الفاصل -->
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="أو المتابعة باستخدام"
                    android:textColor="@color/white"
                    android:textStyle="bold"
                    android:layout_marginBottom="15dp" />

                <!-- أزرار وسائل التواصل الاجتماعي -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_marginBottom="25dp">

                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnGoogle"
                        android:layout_width="0dp"
                        android:layout_height="60dp"
                        android:layout_weight="1"
                        android:text="Google"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/white"
                        android:backgroundTint="@color/colorGoogle"
                        app:cornerRadius="15dp"
                        android:layout_marginEnd="10dp" />

                    <com.google.android.material.button.MaterialButton
                        android:id="@+id/btnFacebook"
                        android:layout_width="0dp"
                        android:layout_height="60dp"
                        android:layout_weight="1"
                        android:text="Facebook"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@color/white"
                        android:backgroundTint="@color/colorFacebook"
                        app:cornerRadius="15dp" />
                </LinearLayout>

                <!-- محول وضع تسجيل الدخول / إنشاء الحساب -->
                <LinearLayout
                    android:id="@+id/switchModeLayout"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/tvSwitchPrefix"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="ليس لديك حساب؟ "
                        android:textColor="@color/white"
                        android:textSize="16sp" />

                    <TextView
                        android:id="@+id/tvSwitchAction"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="إنشاء حساب جديد"
                        android:textColor="@color/blue_primary"
                        android:textStyle="bold"
                        android:textSize="16sp" />
                </LinearLayout>
            </LinearLayout>
        </ScrollView>
    </RelativeLayout>

    <!-- لوحة التحكم الرئيسية (تظهر بعد تسجيل الدخول) -->
    <RelativeLayout
        android:id="@+id/dashboardView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="gone"
        android:background="@color/bg_dark_main">

        <!-- شريط الأدوات -->
        <androidx.appcompat.widget.Toolbar
            android:id="@+id/mainToolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:background="@color/bg_dark_main"
            android:elevation="4dp"
            app:titleTextColor="@color/white" />

        <!-- حاوية المحتوى الرئيسي -->
        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_below="@id/mainToolbar"
            android:layout_above="@id/bottomNavigation">

            <!-- شاشة الإعدادات -->
            <ScrollView
                android:id="@+id/layoutSettings"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:visibility="gone"
                android:padding="15dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:background="@drawable/abc_popup_background_mtrl_mult"
                        android:backgroundTint="@color/bg_dark_card"
                        android:padding="15dp">

                        <!-- عنصر الحساب -->
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="horizontal"
                            android:gravity="center_vertical"
                            android:layout_marginBottom="25dp">

                            <androidx.cardview.widget.CardView
                                android:layout_width="40dp"
                                android:layout_height="40dp"
                                app:cardCornerRadius="20dp"
                                app:cardBackgroundColor="@color/icon_blue"
                                android:layout_marginEnd="15dp">

                                <ImageView
                                    android:layout_width="24dp"
                                    android:layout_height="24dp"
                                    android:src="@android:drawable/ic_menu_myplaces"
                                    app:tint="@color/white"
                                    android:layout_gravity="center" />
                            </androidx.cardview.widget.CardView>

                            <LinearLayout
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:orientation="vertical">

                                <TextView
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="الحساب"
                                    android:textColor="@color/white"
                                    android:textSize="18sp"
                                    android:textStyle="bold" />

                                <TextView
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="الرقم، اسم المستخدم، النبذة"
                                    android:textColor="@color/text_secondary"
                                    android:textSize="14sp" />
                            </LinearLayout>
                        </LinearLayout>

                        <!-- عنصر إعدادات المحادثات -->
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="horizontal"
                            android:gravity="center_vertical"
                            android:layout_marginBottom="25dp">

                            <androidx.cardview.widget.CardView
                                android:layout_width="40dp"
                                android:layout_height="40dp"
                                app:cardCornerRadius="20dp"
                                app:cardBackgroundColor="@color/icon_orange"
                                android:layout_marginEnd="15dp">

                                <ImageView
                                    android:layout_width="24dp"
                                    android:layout_height="24dp"
                                    android:src="@android:drawable/ic_dialog_email"
                                    app:tint="@color/white"
                                    android:layout_gravity="center" />
                            </androidx.cardview.widget.CardView>

                            <LinearLayout
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:orientation="vertical">

                                <TextView
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="إعدادات المحادثات"
                                    android:textColor="@color/white"
                                    android:textSize="18sp"
                                    android:textStyle="bold" />

                                <TextView
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="خلفية الشاشة، الوضع الليلي"
                                    android:textColor="@color/text_secondary"
                                    android:textSize="14sp" />
                            </LinearLayout>
                        </LinearLayout>

                        <!-- عنصر الخصوصية والأمان -->
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="horizontal"
                            android:gravity="center_vertical">

                            <androidx.cardview.widget.CardView
                                android:layout_width="40dp"
                                android:layout_height="40dp"
                                app:cardCornerRadius="20dp"
                                app:cardBackgroundColor="@color/icon_green"
                                android:layout_marginEnd="15dp">

                                <ImageView
                                    android:layout_width="24dp"
                                    android:layout_height="24dp"
                                    android:src="@android:drawable/ic_lock_idle_lock"
                                    app:tint="@color/white"
                                    android:layout_gravity="center" />
                            </androidx.cardview.widget.CardView>

                            <LinearLayout
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:orientation="vertical">

                                <TextView
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="الخصوصية والأمان"
                                    android:textColor="@color/white"
                                    android:textSize="18sp"
                                    android:textStyle="bold" />

                                <TextView
                                    android:layout_width="wrap_content"
                                    android:layout_height="wrap_content"
                                    android:text="آخر ظهور، الأجهزة، مفاتيح المرور"
                                    android:textColor="@color/text_secondary"
                                    android:textSize="14sp" />
                            </LinearLayout>
                        </LinearLayout>
                    </LinearLayout>
                </LinearLayout>
            </ScrollView>

            <!-- شاشة الملف الشخصي -->
            <ScrollView
                android:id="@+id/layoutProfile"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:visibility="gone">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center_horizontal"
                    android:paddingTop="20dp">

                    <!-- صورة الملف الشخصي -->
                    <androidx.cardview.widget.CardView
                        android:layout_width="100dp"
                        android:layout_height="100dp"
                        app:cardCornerRadius="50dp"
                        app:cardBackgroundColor="@color/bg_dark_card">

                        <ImageView
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            android:src="@drawable/bg_login"
                            android:scaleType="centerCrop" />
                    </androidx.cardview.widget.CardView>

                    <!-- اسم المستخدم -->
                    <TextView
                        android:id="@+id/profileName"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="HUSSEIN •"
                        android:textColor="@color/white"
                        android:textSize="26sp"
                        android:textStyle="bold"
                        android:layout_marginTop="10dp" />

                    <!-- حالة الاتصال -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="متصل"
                        android:textColor="@color/icon_blue"
                        android:textSize="16sp"
                        android:layout_marginBottom="20dp" />

                    <!-- أزرار الإجراءات -->
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:paddingHorizontal="15dp"
                        android:layout_marginBottom="20dp">

                        <com.google.android.material.button.MaterialButton
                            android:layout_width="0dp"
                            android:layout_height="55dp"
                            android:layout_weight="1"
                            android:text="الإعدادات"
                            android:textColor="@color/white"
                            android:backgroundTint="@color/bg_dark_card"
                            app:cornerRadius="15dp"
                            android:layout_marginEnd="10dp" />

                        <com.google.android.material.button.MaterialButton
                            android:layout_width="0dp"
                            android:layout_height="55dp"
                            android:layout_weight="1"
                            android:text="تعديل البيانات"
                            android:textColor="@color/white"
                            android:backgroundTint="@color/bg_dark_card"
                            app:cornerRadius="15dp"
                            android:layout_marginEnd="10dp" />

                        <com.google.android.material.button.MaterialButton
                            android:layout_width="0dp"
                            android:layout_height="55dp"
                            android:layout_weight="1"
                            android:text="تعيين صورة"
                            android:textColor="@color/white"
                            android:backgroundTint="@color/bg_dark_card"
                            app:cornerRadius="15dp" />
                    </LinearLayout>

                    <!-- معلومات المستخدم -->
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:background="@color/bg_dark_card"
                        android:padding="20dp"
                        android:layout_marginHorizontal="15dp"
                        android:layout_marginBottom="20dp">

                        <TextView
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="#لست صداعاً انا فكرة اكبر من رأسك."
                            android:textColor="@color/white"
                            android:textSize="18sp"
                            android:gravity="right"
                            android:layout_marginBottom="5dp" />

                        <TextView
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="النبذة"
                            android:textColor="@color/text_secondary"
                            android:textSize="14sp"
                            android:gravity="right"
                            android:layout_marginBottom="15dp" />

                        <TextView
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="@iomk0"
                            android:textColor="@color/white"
                            android:textSize="18sp"
                            android:gravity="right"
                            android:layout_marginBottom="5dp" />

                        <TextView
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="اسم المستخدم"
                            android:textColor="@color/text_secondary"
                            android:textSize="14sp"
                            android:gravity="right" />
                    </LinearLayout>

                    <!-- زر إضافة منشور -->
                    <com.google.android.material.button.MaterialButton
                        android:layout_width="wrap_content"
                        android:layout_height="60dp"
                        android:text="إضافة منشور"
                        android:textColor="@color/white"
                        android:backgroundTint="@color/icon_blue"
                        app:cornerRadius="30dp"
                        app:icon="@android:drawable/ic_menu_camera"
                        app:iconTint="@color/white"
                        android:layout_marginBottom="30dp" />
                </LinearLayout>
            </ScrollView>

            <!-- شاشة المحادثات (مؤقتة) -->
            <TextView
                android:id="@+id/layoutChats"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:text="قائمة المحادثات ستظهر هنا قريباً"
                android:textColor="@color/white"
                android:gravity="center"
                android:textSize="20sp"
                android:visibility="visible" />
        </FrameLayout>

        <!-- شريط التنقل السفلي -->
        <com.google.android.material.bottomnavigation.BottomNavigationView
            android:id="@+id/bottomNavigation"
            android:layout_width="match_parent"
            android:layout_height="70dp"
            android:layout_alignParentBottom="true"
            android:background="@color/bg_dark_card"
            android:elevation="20dp"
            app:menu="@menu/bottom_nav"
            app:itemTextColor="@color/text_secondary"
            app:itemIconTint="@color/text_secondary" />
    </RelativeLayout>
</RelativeLayout>

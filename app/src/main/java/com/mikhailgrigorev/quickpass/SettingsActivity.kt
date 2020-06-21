package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {

    private val KEY_THEME = "themePreference"
    private val KEY_THEME_ACCENT = "themeAccentPreference"
    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USERNAME = "prefUserNameKey"
    private val KEY_BIO = "prefUserBioKey"
    private val KEY_AUTOCOPY = "prefAutoCopyKey"
    private val KEY_USEPIN = "prefUsePinKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var account: String
    private lateinit var imageName: String
    @SuppressLint("SetTextI18n", "Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        when(pref.getString(KEY_THEME, "none")){
            "none", "yes" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "no" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "battery" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
        when(pref.getString("themeAccentPreference", "none")){
            "Red" -> setTheme(R.style.AppThemeRed)
            "Pink" -> setTheme(R.style.AppThemePink)
            "Purple" -> setTheme(R.style.AppThemePurple)
            "Violet" -> setTheme(R.style.AppThemeViolet)
            "DViolet" -> setTheme(R.style.AppThemeDarkViolet)
            "Blue" -> setTheme(R.style.AppThemeBlue)
            "Cyan" -> setTheme(R.style.AppThemeCyan)
            "Teal" -> setTheme(R.style.AppThemeTeal)
            "Green" -> setTheme(R.style.AppThemeGreen)
            "LGreen" -> setTheme(R.style.AppThemeLightGreen)
            else -> setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)

        when ((resources.configuration.uiMode + Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_NO ->
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_settings)

        when(AppCompatDelegate.getDefaultNightMode()){
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> defaultSystem.isChecked = true
            AppCompatDelegate.MODE_NIGHT_NO -> light.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> dark.isChecked = true
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> autoBattery.isChecked = true
            AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> defaultSystem.isChecked = true
            else-> defaultSystem.isChecked = true
        }


        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        passName = args?.get("passName").toString()
        account = args?.get("activity").toString()

        // Checking prefs
        val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

        with(sharedPref.edit()) {
            putString(KEY_USERNAME, login)
            commit()
        }

        //THEME
        // Получаем экземпляр элемента Spinner

        light.setOnClickListener {
            with(sharedPref.edit()) {
                putString(KEY_THEME, "no")
                commit()
            }
            light.isChecked = true
            recreate()
        }
        dark.setOnClickListener {
            with(sharedPref.edit()) {
                putString(KEY_THEME, "yes")
                commit()
            }
            dark.isChecked = true
            recreate()
        }
        autoBattery.setOnClickListener {
            with(sharedPref.edit()) {
                putString(KEY_THEME, "battery")
                commit()
            }
            autoBattery.isChecked = true
            recreate()
        }
        defaultSystem.setOnClickListener {
            with(sharedPref.edit()) {
                putString(KEY_THEME, "default")
                commit()
            }
            defaultSystem.isChecked = true
            recreate()
        }


        val useBio = sharedPref.getString(KEY_BIO, "none")
        val useAuto = sharedPref.getString(KEY_AUTOCOPY, "none")
        val usePin = sharedPref.getString(KEY_USEPIN, "none")

        if(useBio == "using"){
            biometricSwitch.isChecked = true
        }

        if(useAuto == "dis"){
            autoCopySwitch.isChecked = false
        }

        if(usePin != "none"){
            setPinSwitch.isChecked = true
        }


        autoCopySwitch.setOnCheckedChangeListener { _, _ ->
            if(!autoCopySwitch.isChecked){
                with (sharedPref.edit()) {
                    putString(KEY_AUTOCOPY, "dis")
                    commit()
                }
            }
            else{
                with (sharedPref.edit()) {
                    putString(KEY_AUTOCOPY, "none")
                    commit()
                }
            }
        }

        autoCopy.setOnClickListener {
            if(autoCopySwitch.isChecked){
                autoCopySwitch.isChecked = false
                with (sharedPref.edit()) {
                    putString(KEY_AUTOCOPY, "dis")
                    commit()
                }
            }
            else{
                autoCopySwitch.isChecked = true
                with (sharedPref.edit()) {
                    putString(KEY_AUTOCOPY, "none")
                    commit()
                }
            }
        }

        setPinSwitch.setOnCheckedChangeListener { _, _ ->
            if(setPinSwitch.isChecked){
                val intent = Intent(this, SetPinActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                intent.putExtra("activity", account)
                startActivity(intent)
                finish()
            }
            else{
                with (sharedPref.edit()) {
                    putString(KEY_USEPIN, "none")
                    commit()
                }
            }
        }

        setPin.setOnClickListener {
            if(setPinSwitch.isChecked){
                setPinSwitch.isChecked = false
                with (sharedPref.edit()) {
                    putString(KEY_USEPIN, "none")
                    commit()
                }
            }
            else{
                val intent = Intent(this, SetPinActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                intent.putExtra("activity", account)
                startActivity(intent)
                finish()
            }
        }

        biometricSwitch.setOnCheckedChangeListener { _, _ ->
            if(biometricSwitch.isChecked){
                with (sharedPref.edit()) {
                    putString(KEY_BIO, "using")
                    commit()
                }
            }
            else{
                with (sharedPref.edit()) {
                    putString(KEY_BIO, "none")
                    commit()
                }
            }
        }

        biometricText.setOnClickListener {
            if(!biometricSwitch.isChecked){
                biometricSwitch.isChecked = true
                with (sharedPref.edit()) {
                    putString(KEY_BIO, "using")
                    commit()
                }
            }
            else{
                biometricSwitch.isChecked = false
                with (sharedPref.edit()) {
                    putString(KEY_BIO, "none")
                    commit()
                }
            }
        }


        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
                dbHelper.TABLE_USERS,
                arrayOf(dbHelper.KEY_NAME, dbHelper.KEY_PASS, dbHelper.KEY_ID, dbHelper.KEY_IMAGE),
                "NAME = ?",
                arrayOf(login),
                null,
                null,
                null
        )

        if (cursor.moveToFirst()) {
            val imageIndex: Int = cursor.getColumnIndex(dbHelper.KEY_IMAGE)
            do {
                val exInfoImgText = cursor.getString(imageIndex).toString()
                imageName = exInfoImgText
                when(cursor.getString(imageIndex).toString()){
                    "ic_account" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account)
                    "ic_account_Pink" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Pink)
                    "ic_account_Red" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Red)
                    "ic_account_Purple" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Purple)
                    "ic_account_Violet" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Violet)
                    "ic_account_Dark_Violet" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Dark_Violet)
                    "ic_account_Blue" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Blue)
                    "ic_account_Cyan" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Cyan)
                    "ic_account_Teal" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Teal)
                    "ic_account_Green" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Green)
                    "ic_account_lightGreen" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_lightGreen)
                    else -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account)
                }
                accountAvatarText.text = login[0].toString()
            } while (cursor.moveToNext())
        }


// Checking prefs
        updateChooser(imageName)


        redColor.setOnClickListener {
            imageName = "ic_account_Red"
            updateChooser(imageName)
        }

        pinkColor.setOnClickListener {
            imageName = "ic_account_Pink"
            updateChooser(imageName)
        }

        purpleColor.setOnClickListener {
            imageName = "ic_account_Purple"
            updateChooser(imageName)
        }

        violetColor.setOnClickListener {
            imageName = "ic_account_Violet"
            updateChooser(imageName)
        }

        darkVioletColor.setOnClickListener {
            imageName = "ic_account_Dark_Violet"
            updateChooser(imageName)
        }

        blueColor.setOnClickListener {
            imageName = "ic_account_Blue"
            updateChooser(imageName)
        }

        cyanColor.setOnClickListener {
            imageName = "ic_account_Cyan"
            updateChooser(imageName)
        }

        tealColor.setOnClickListener {
            imageName = "ic_account_Teal"
            updateChooser(imageName)
        }
        greenColor.setOnClickListener {
            imageName = "ic_account_Green"
            updateChooser(imageName)
        }
        lightGreenColor.setOnClickListener {
            imageName = "ic_account_lightGreen"
            updateChooser(imageName)
        }

    }


    private fun updateChooser(imageName: String){
        when (imageName) {
            "ic_account_Red" -> {
                clearCE()
                setAlpha()
                redColor.alpha = 1F
                redColor.cardElevation = 20F
            }
            "ic_account_Pink" -> {
                clearCE()
                setAlpha()
                pinkColor.alpha = 1F
                pinkColor.cardElevation = 20F
            }
            "ic_account_Purple" -> {
                clearCE()
                setAlpha()
                purpleColor.alpha = 1F
                purpleColor.cardElevation = 20F
            }
            "ic_account_Violet" -> {
                clearCE()
                setAlpha()
                violetColor.alpha = 1F
                violetColor.cardElevation = 20F
            }
            "ic_account_Dark_Violet" -> {
                clearCE()
                setAlpha()
                darkVioletColor.alpha = 1F
                darkVioletColor.cardElevation = 20F
            }
            "ic_account_Blue" -> {
                clearCE()
                setAlpha()
                blueColor.alpha = 1F
                blueColor.cardElevation = 20F
            }
            "ic_account_Cyan" -> {
                clearCE()
                setAlpha()
                cyanColor.alpha = 1F
                cyanColor.cardElevation = 20F
            }
            "ic_account_Teal" -> {
                clearCE()
                setAlpha()
                tealColor.alpha = 1F
                tealColor.cardElevation = 20F
            }
            "ic_account_Green" -> {
                clearCE()
                setAlpha()
                greenColor.alpha = 1F
                greenColor.cardElevation = 20F
            }
            "ic_account_lightGreen" -> {
                clearCE()
                setAlpha()
                lightGreenColor.alpha = 1F
                lightGreenColor.cardElevation = 20F
            }
            else ->  {
                clearCE()
                setAlpha()
                purpleColor.alpha = 1F
                purpleColor.cardElevation = 20F
            }
        }
        updateAvatar(imageName)
    }

    private fun clearCE() {
        redColor.cardElevation = 0F
        pinkColor.cardElevation = 0F
        purpleColor.cardElevation = 0F
        violetColor.cardElevation = 0F
        darkVioletColor.cardElevation = 0F
        blueColor.cardElevation = 0F
        cyanColor.cardElevation = 0F
        tealColor.cardElevation = 0F
        greenColor.cardElevation = 0F
        lightGreenColor.cardElevation = 0F
    }
    private fun setAlpha() {
        redColor.alpha = 0.7F
        pinkColor.alpha = 0.7F
        purpleColor.alpha = 0.7F
        violetColor.alpha = 0.7F
        darkVioletColor.alpha = 0.7F
        blueColor.alpha = 0.7F
        cyanColor.alpha = 0.7F
        tealColor.alpha = 0.7F
        greenColor.alpha = 0.7F
        lightGreenColor.alpha = 0.7F
    }

    private fun updateAvatar(imageName: String) {

        when(imageName){
            "ic_account" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account)
                // Checking prefs
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                if(sharedPref.getString("themeAccentPreference", "none") != "Violet") {
                    with(sharedPref.edit()) {
                        putString(KEY_THEME_ACCENT, "Violet")
                        commit()
                    }
                    recreate()
                }
            }
            "ic_account_Pink" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                    this, R.color.ic_account_Pink)
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                if(sharedPref.getString("themeAccentPreference", "none") != "Pink") {
                    with(sharedPref.edit()) {
                        putString(KEY_THEME_ACCENT, "Pink")
                        commit()
                    }
                    recreate()
                }
            }
            "ic_account_Red" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Red)
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                if(sharedPref.getString("themeAccentPreference", "none") != "Red") {
                    with(sharedPref.edit()) {
                        putString(KEY_THEME_ACCENT, "Red")
                        commit()
                    }
                    recreate()
                }
            }
            "ic_account_Purple" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Purple)
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                if(sharedPref.getString("themeAccentPreference", "none") != "Purple") {
                    with(sharedPref.edit()) {
                        putString(KEY_THEME_ACCENT, "Purple")
                        commit()
                    }
                    recreate()
                }
            }
            "ic_account_Violet" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Violet)
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                if(sharedPref.getString("themeAccentPreference", "none") != "Violet") {
                with(sharedPref.edit()) {
                    putString(KEY_THEME_ACCENT, "Violet")
                    commit()
                }
                    recreate()
                }
            }
            "ic_account_Dark_Violet" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Dark_Violet)
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                    if(sharedPref.getString("themeAccentPreference", "none") != "DViolet") {
                with(sharedPref.edit()) {
                    putString(KEY_THEME_ACCENT, "DViolet")
                    commit()
                }
                        recreate()
                    }
            }
            "ic_account_Blue" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Blue)
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                    if(sharedPref.getString("themeAccentPreference", "none") != "Blue") {
                with(sharedPref.edit()) {
                    putString(KEY_THEME_ACCENT, "Blue")
                    commit()
                }
                        recreate()
                    }
            }
            "ic_account_Cyan" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Cyan)
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                    if(sharedPref.getString("themeAccentPreference", "none") != "Cyan") {
                with(sharedPref.edit()) {
                    putString(KEY_THEME_ACCENT, "Cyan")
                    commit()
                }
                        recreate()
                    }
            }
            "ic_account_Teal" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Teal)
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                    if(sharedPref.getString("themeAccentPreference", "none") != "Teal") {
                with(sharedPref.edit()) {
                    putString(KEY_THEME_ACCENT, "Teal")
                    commit()
                }
                        recreate()
                    }
            }
            "ic_account_Green" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_Green)
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                    if(sharedPref.getString("themeAccentPreference", "none") != "Green") {
                with(sharedPref.edit()) {
                    putString(KEY_THEME_ACCENT, "Green")
                    commit()
                }
                        recreate()
                    }
            }
            "ic_account_lightGreen" -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account_lightGreen)
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                    if(sharedPref.getString("themeAccentPreference", "none") != "LGreen") {
                with(sharedPref.edit()) {
                    putString(KEY_THEME_ACCENT, "LGreen")
                    commit()
                }
                        recreate()
                    }
            }
            else -> {
                accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                        this, R.color.ic_account)
                val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                    if(sharedPref.getString("themeAccentPreference", "none") != "Violet") {
                with(sharedPref.edit()) {
                    putString(KEY_THEME_ACCENT, "Violet")
                    commit()
                }
                        recreate()
                    }
            }
        }

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(dbHelper.KEY_IMAGE, imageName)
        database.update(
                dbHelper.TABLE_USERS, contentValues,
                "NAME = ?",
                arrayOf(login)
        )
    }

    /*
        val pdbHelper = PasswordsDataBaseHelper(this, login)
        val pDatabase = pdbHelper.writableDatabase
        try {
            val pCursor: Cursor = pDatabase.query(
                pdbHelper.TABLE_USERS, arrayOf(pdbHelper.KEY_NAME, pdbHelper.KEY_PASS,
                    pdbHelper.KEY_2FA, pdbHelper.KEY_TAGS),
                null, null,
                null, null, null
            )
*/

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val intent = Intent(this, AccountActivity::class.java)
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                intent.putExtra("activity", account)
                startActivity(intent)
                this.overridePendingTransition(
                        R.anim.right_in,
                        R.anim.right_out
                )
                finish()
            }
        }
        return false
    }
}
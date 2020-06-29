package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.database.Cursor
import android.database.SQLException
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_pass_gen.*
import java.util.*
import kotlin.collections.ArrayList


class PassGenActivity : AppCompatActivity() {

    private val KEY_THEME = "themePreference"
    private val PREFERENCE_FILE_KEY = "quickPassPreference"
    private val KEY_USERNAME = "prefUserNameKey"
    private var length = 20
    private var useSymbols = false
    private var useUC = false
    private var useLetters = false
    private var useNumbers = false
    private var safePass = 0
    private var unsafePass = 0
    private var fixPass = 0
    private val passwords: ArrayList<Pair<String, String>> = ArrayList()
    private var passwordsG: ArrayList<Pair<String, String>> = ArrayList()
    private val realPass: ArrayList<Pair<String, String>> = ArrayList()
    private val realQuality: ArrayList<String> = ArrayList()
    private val realMap: MutableMap<String, ArrayList<String>> = mutableMapOf()
    private val quality: ArrayList<String> = ArrayList()
    private val tags: ArrayList<String> = ArrayList()
    private val group: ArrayList<String> = ArrayList()
    private lateinit var login: String

    private var searchPos: Boolean = false
    private var searchNeg: Boolean = false
    private var searchMId: Boolean = false


    @RequiresApi(Build.VERSION_CODES.N_MR1)
    @SuppressLint("Recycle", "ClickableViewAccessibility", "ResourceAsColor", "RestrictedApi",
        "SetTextI18n"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        when(pref.getString(KEY_THEME, "none")){
            "yes" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "no" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "none", "default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
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
        setContentView(R.layout.activity_pass_gen)


        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        val name: String? = getString(R.string.hi) + " " + login
        helloTextId.text = name

        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
            dbHelper.TABLE_USERS, arrayOf(dbHelper.KEY_IMAGE),
            "NAME = ?", arrayOf(login),
            null, null, null
        )
        if (cursor.moveToFirst()) {
            val imageIndex: Int = cursor.getColumnIndex(dbHelper.KEY_IMAGE)
            do {
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


        var dbLogin: String

        val pdbHelper = PasswordsDataBaseHelper(this, login)
        val pDatabase = pdbHelper.writableDatabase
        try {
            val pCursor: Cursor = pDatabase.query(
                pdbHelper.TABLE_USERS, arrayOf(pdbHelper.KEY_NAME, pdbHelper.KEY_PASS,
                    pdbHelper.KEY_2FA, pdbHelper.KEY_TAGS, pdbHelper.KEY_GROUPS),
                null, null,
                null, null, null
            )

            if (pCursor.moveToFirst()) {
                val nameIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_NAME)
                val passIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_PASS)
                do {
                    val pass = pCursor.getString(passIndex).toString()
                    val login = pCursor.getString(nameIndex).toString()
                    realPass.add(Pair(login, pass))
                } while (pCursor.moveToNext())
            }

            analyzeDataBase()

            if (pCursor.moveToFirst()) {
                val nameIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_NAME)
                val passIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_PASS)
                val aIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_2FA)
                val tagsIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_TAGS)
                val groupIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_GROUPS)
                var j = 0
                do {
                    val pass = pCursor.getString(passIndex).toString()
                    val myPasswordManager = PasswordManager()
                    val evaluation: Float =
                        myPasswordManager.evaluatePassword(pass)
                    var qualityNum = when {
                        evaluation < 0.33 -> "2"
                        evaluation < 0.66 -> "3"
                        else -> "1"
                    }
                    if(realQuality[j] != "1")
                        qualityNum = "2"
                    j++
                    if(pCursor.getString(groupIndex) == null || pCursor.getString(groupIndex) == "none") {
                        dbLogin = pCursor.getString(nameIndex).toString()
                        val fa = pCursor.getString(aIndex).toString()
                        passwords.add(Pair(dbLogin, fa))
                        quality.add(qualityNum)
                        val dbTag = pCursor.getString(tagsIndex).toString()
                        tags.add(dbTag)
                        group.add("none")
                    }
                    else {
                        dbLogin = pCursor.getString(nameIndex).toString()
                        val fa = pCursor.getString(aIndex).toString()
                        passwords.add(0, Pair(dbLogin, fa))
                        quality.add(0, qualityNum)
                        val dbTag = pCursor.getString(tagsIndex).toString()
                        tags.add(0, dbTag)
                        group.add(0, "#favorite")
                    }


                    when (qualityNum) {
                        "1" -> safePass += 1
                        "2" -> unsafePass += 1
                        "3" -> fixPass += 1
                    }
                } while (pCursor.moveToNext())
            }
        } catch (e: SQLException) {
        }

        if(passwords.size > 0) {
            val intent = Intent(this, PasswordViewActivity::class.java)
            var isPass = false
            intent.action = Intent.ACTION_VIEW
            intent.putExtra("login", login)
            intent.putExtra("passName", passwords[0].first)
            var str = getString(R.string.sameParts)
            if (realMap.containsKey(passwords[0].first)){
                for(pass in realMap[passwords[0].first]!!) {
                    isPass = true
                    str += "$pass "
                }
            }
            if(isPass)
                intent.putExtra("sameWith", str)
            else
                intent.putExtra("sameWith", "none")

            val shortcutManager: ShortcutManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                shortcutManager = getSystemService<ShortcutManager>(ShortcutManager::class.java)!!
                val shortcut: ShortcutInfo
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    shortcut = ShortcutInfo.Builder(this, "first_shortcut")
                            .setShortLabel(passwords[0].first)
                            .setLongLabel(passwords[0].first)
                            .setIcon(Icon.createWithResource(this, R.drawable.ic_fav_action))
                            .setIntent(intent)
                            .build()
                    shortcutManager.dynamicShortcuts = listOf(shortcut)
                }
            }
        }

        if(passwords.size > 1) {
            val intent = Intent(this, PasswordViewActivity::class.java)
            var isPass = false
            intent.action = Intent.ACTION_VIEW
            intent.putExtra("login", login)
            intent.putExtra("passName", passwords[1].first)
            var str = getString(R.string.sameParts)
            if (realMap.containsKey(passwords[1].first)){
                for(pass in realMap[passwords[1].first]!!) {
                    isPass = true
                    str += "$pass "
                }
            }
            if(isPass)
                intent.putExtra("sameWith", str)
            else
                intent.putExtra("sameWith", "none")

            val shortcutManager: ShortcutManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                shortcutManager = getSystemService<ShortcutManager>(ShortcutManager::class.java)!!
                val shortcut: ShortcutInfo
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    shortcut = ShortcutInfo.Builder(this, "second_shortcut")
                            .setShortLabel(passwords[1].first)
                            .setLongLabel(passwords[1].first)
                            .setIcon(Icon.createWithResource(this, R.drawable.ic_fav_action2))
                            .setIntent(intent)
                            .build()
                    shortcutManager.dynamicShortcuts = shortcutManager.dynamicShortcuts +  listOf(shortcut)
                }
            }
        }
        if(passwords.size > 2) {
            val intent = Intent(this, PasswordViewActivity::class.java)
            var isPass = false
            intent.action = Intent.ACTION_VIEW
            intent.putExtra("login", login)
            intent.putExtra("passName", passwords[2].first)
            var str = getString(R.string.sameParts)
            if (realMap.containsKey(passwords[2].first)){
                for(pass in realMap[passwords[2].first]!!) {
                    isPass = true
                    str += "$pass "
                }
            }
            if(isPass)
                intent.putExtra("sameWith", str)
            else
                intent.putExtra("sameWith", "none")

            val shortcutManager: ShortcutManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                shortcutManager = getSystemService<ShortcutManager>(ShortcutManager::class.java)!!
                val shortcut: ShortcutInfo
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    shortcut = ShortcutInfo.Builder(this, "third_shortcut")
                            .setShortLabel(passwords[2].first)
                            .setLongLabel(passwords[2].first)
                            .setIcon(Icon.createWithResource(this, R.drawable.ic_fav_action3))
                            .setIntent(intent)
                            .build()
                    shortcutManager.dynamicShortcuts = shortcutManager.dynamicShortcuts + listOf(shortcut)
                }
            }
        }
        if(passwords.size == 0){
            allPassword.visibility = View.GONE
            noPasswords.visibility = View.VISIBLE
        }

        correctPasswords.text = resources.getQuantityString(R.plurals.correct_passwords, safePass, safePass)
        negativePasswords.text = resources.getQuantityString(R.plurals.incorrect_password, unsafePass, unsafePass)
        fixPasswords.text = resources.getQuantityString(R.plurals.need_fix, fixPass, fixPass)

        passwordRecycler.layoutManager = LinearLayoutManager(this,  LinearLayoutManager.VERTICAL, false)

        passwordRecycler.setHasFixedSize(true)

        passwordsG = passwords
        passwordRecycler.adapter = PasswordAdapter(passwords, quality, tags, group, this, clickListener = {
            passClickListener(it)
        })

        positiveCircle.setOnClickListener {
            if(searchPos){
                positiveCircle.setImageResource(R.drawable.circle_positive)

                passwordsG = passwords
                passwordRecycler.adapter = PasswordAdapter(passwords, quality, tags, group, this, clickListener = {
                    passClickListener(it)
                })
                searchPos = false
            }
            else{
                searchNeg = false
                searchMId= false
                negativeCircle.setImageResource(R.drawable.circle_negative)
                negativeCircle2.setImageResource(R.drawable.circle_improvement)
                positiveCircle.setImageResource(R.drawable.circle_positive_fill)
                val passwords2: ArrayList<Pair<String, String>> = ArrayList()
                val quality2: ArrayList<String> = ArrayList()
                val tags2: ArrayList<String> = ArrayList()
                val group2: ArrayList<String> = ArrayList()
                for ((index, value) in quality.withIndex()) {
                    if (value == "1"){
                        passwords2.add(passwords[index])
                        quality2.add(quality[index])
                        tags2.add(tags[index])
                        group2.add(group[index])
                    }
                }

                passwordsG = passwords2
                passwordRecycler.adapter = PasswordAdapter(passwords2, quality2, tags2, group2,this@PassGenActivity, clickListener = {
                    passClickListener(it)
                })
                searchPos = true
            }
        }

        correctPasswords.setOnClickListener{
            if(searchPos){
                positiveCircle.setImageResource(R.drawable.circle_positive)
                passwordsG = passwords
                passwordRecycler.adapter = PasswordAdapter(passwords, quality, tags, group, this, clickListener = {
                    passClickListener(it)
                })
                searchPos = false
            }
            else{
                searchMId = false
                searchNeg = false
                negativeCircle.setImageResource(R.drawable.circle_negative)
                negativeCircle2.setImageResource(R.drawable.circle_improvement)
                positiveCircle.setImageResource(R.drawable.circle_positive_fill)
                val passwords2: ArrayList<Pair<String, String>> = ArrayList()
                val quality2: ArrayList<String> = ArrayList()
                val tags2: ArrayList<String> = ArrayList()
                val group2: ArrayList<String> = ArrayList()
                for ((index, value) in quality.withIndex()) {
                    if (value == "1"){
                        passwords2.add(passwords[index])
                        quality2.add(quality[index])
                        tags2.add(tags[index])
                        group2.add(group[index])
                    }
                }

                passwordsG = passwords2
                passwordRecycler.adapter = PasswordAdapter(passwords2, quality2, tags2, group2,this@PassGenActivity, clickListener = {
                    passClickListener(it)
                })
                searchPos = true
            }
        }

        negativeCircle.setOnClickListener {
            if(searchNeg){
                negativeCircle.setImageResource(R.drawable.circle_negative)

                passwordsG = passwords
                passwordRecycler.adapter = PasswordAdapter(passwords, quality, tags,group,this, clickListener = {
                    passClickListener(it)
                })
                searchNeg = false
            }
            else{
                searchMId = false
                searchPos = false
                positiveCircle.setImageResource(R.drawable.circle_positive)
                negativeCircle2.setImageResource(R.drawable.circle_improvement)
                negativeCircle.setImageResource(R.drawable.circle_negative_fill)
                val passwords2: ArrayList<Pair<String, String>> = ArrayList()
                val quality2: ArrayList<String> = ArrayList()
                val tags2: ArrayList<String> = ArrayList()
                val group2: ArrayList<String> = ArrayList()
                for ((index, value) in quality.withIndex()) {
                    if (value == "2"){
                        passwords2.add(passwords[index])
                        quality2.add(quality[index])
                        tags2.add(tags[index])
                        group2.add(group[index])
                    }
                }

                passwordsG = passwords2
                passwordRecycler.adapter = PasswordAdapter(passwords2, quality2, tags2, group,this@PassGenActivity, clickListener = {
                    passClickListener(it)
                })
                searchNeg = true
            }
        }

        negativePasswords.setOnClickListener{
            if(searchNeg){
                negativeCircle.setImageResource(R.drawable.circle_negative)

                passwordsG = passwords
                passwordRecycler.adapter = PasswordAdapter(passwords, quality, tags,group,this, clickListener = {
                    passClickListener(it)
                })
                searchNeg = false
            }
            else{
                searchMId = false
                searchPos = false
                positiveCircle.setImageResource(R.drawable.circle_positive)
                negativeCircle2.setImageResource(R.drawable.circle_improvement)
                negativeCircle.setImageResource(R.drawable.circle_negative_fill)
                val passwords2: ArrayList<Pair<String, String>> = ArrayList()
                val quality2: ArrayList<String> = ArrayList()
                val tags2: ArrayList<String> = ArrayList()
                val group2: ArrayList<String> = ArrayList()
                for ((index, value) in quality.withIndex()) {
                    if (value == "2"){
                        passwords2.add(passwords[index])
                        quality2.add(quality[index])
                        tags2.add(tags[index])
                        group2.add(group[index])
                    }
                }

                passwordsG = passwords2
                passwordRecycler.adapter = PasswordAdapter(passwords2, quality2, tags2,group, this@PassGenActivity, clickListener = {
                    passClickListener(it)
                })
                searchNeg = true
            }
        }


        negativeCircle2.setOnClickListener{
            if(searchMId){
                negativeCircle2.setImageResource(R.drawable.circle_improvement)

                passwordsG = passwords
                passwordRecycler.adapter = PasswordAdapter(passwords, quality, tags,group,this, clickListener = {
                    passClickListener(it)
                })
                searchMId = false
            }
            else{
                searchNeg = false
                searchPos = false
                positiveCircle.setImageResource(R.drawable.circle_positive)
                negativeCircle.setImageResource(R.drawable.circle_negative)
                negativeCircle2.setImageResource(R.drawable.circle_improvement_fill)
                val passwords2: ArrayList<Pair<String, String>> = ArrayList()
                val quality2: ArrayList<String> = ArrayList()
                val tags2: ArrayList<String> = ArrayList()
                val group2: ArrayList<String> = ArrayList()
                for ((index, value) in quality.withIndex()) {
                    if (value == "3"){
                        passwords2.add(passwords[index])
                        quality2.add(quality[index])
                        tags2.add(tags[index])
                        group2.add(group[index])
                    }
                }

                passwordsG = passwords2
                passwordRecycler.adapter = PasswordAdapter(passwords2, quality2, tags2, group2,this@PassGenActivity, clickListener = {
                    passClickListener(it)
                })
                searchMId = true
            }
        }

        fixPasswords.setOnClickListener {
            if(searchMId){
                negativeCircle2.setImageResource(R.drawable.circle_improvement)

                passwordsG = passwords
                passwordRecycler.adapter = PasswordAdapter(passwords, quality, tags,group,this, clickListener = {
                    passClickListener(it)
                })
                searchMId = false
            }
            else{
                searchNeg = false
                searchPos = false
                positiveCircle.setImageResource(R.drawable.circle_positive)
                negativeCircle.setImageResource(R.drawable.circle_negative)
                negativeCircle2.setImageResource(R.drawable.circle_improvement_fill)
                val passwords2: ArrayList<Pair<String, String>> = ArrayList()
                val quality2: ArrayList<String> = ArrayList()
                val tags2: ArrayList<String> = ArrayList()
                val group2: ArrayList<String> = ArrayList()
                for ((index, value) in quality.withIndex()) {
                    if (value == "3"){
                        passwords2.add(passwords[index])
                        quality2.add(quality[index])
                        tags2.add(tags[index])
                        group2.add(group[index])
                    }
                }

                passwordsG = passwords2
                passwordRecycler.adapter = PasswordAdapter(passwords2, quality2, tags2, group2,this@PassGenActivity, clickListener = {
                    passClickListener(it)
                })
                searchMId = true
            }
        }


        search.setOnClickListener {
            if(searchPass.visibility ==  View.GONE){
                searchMId = false
                searchNeg = false
                searchPos = false
                positiveCircle.setImageResource(R.drawable.circle_positive)
                negativeCircle.setImageResource(R.drawable.circle_negative)
                negativeCircle2.setImageResource(R.drawable.circle_improvement)
                newPass.hide()
                showAll.hide()
                searchPass.visibility = View.VISIBLE
                imageView.visibility = View.VISIBLE

            }
            else{
                newPass.show()
                showAll.show()
                searchPass.visibility = View.GONE
                imageView.visibility = View.GONE
                passwordsG = passwords
                passwordRecycler.adapter = PasswordAdapter(passwords, quality, tags,group, this, clickListener = {
                    passClickListener(it)
                })
            }
        }

        searchPassField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val passwords2: ArrayList<Pair<String, String>> = ArrayList()
                val quality2: ArrayList<String> = ArrayList()
                val tags2: ArrayList<String> = ArrayList()
                val group2: ArrayList<String> = ArrayList()
                for ((index, pair) in passwords.withIndex()) {
                    if (pair.first.toLowerCase(Locale.ROOT).contains(s.toString().toLowerCase(Locale.ROOT)) ||
                        tags[index].toLowerCase(Locale.ROOT).contains(s.toString().toLowerCase(Locale.ROOT))){
                        passwords2.add(pair)
                        quality2.add(quality[index])
                        tags2.add(tags[index])
                        group2.add(group[index])
                    }
                }

                passwordsG = passwords2
                passwordRecycler.adapter = PasswordAdapter(passwords2, quality2, tags2, group2,this@PassGenActivity, clickListener = {
                    passClickListener(it)
                })
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })


        // Checking prefs
        val sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

        with (sharedPref.edit()) {
            putString(KEY_USERNAME, login)
            commit()
        }

        accountAvatar.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("activity","menu")
            startActivity(intent)
            finish()
        }

        val list = mutableListOf<String>()
        // Loop through the chips
        for (index in 0 until passSettings.childCount) {
            val chip: Chip = passSettings.getChildAt(index) as Chip

            // Set the chip checked change listener
            chip.setOnCheckedChangeListener{view, isChecked ->
                val deg = generatePassword.rotation + 30f
                generatePassword.animate().rotation(deg).interpolator = AccelerateDecelerateInterpolator()
                if (isChecked){
                    if (view.id == R.id.lettersToggle)
                        useLetters = true
                    if (view.id == R.id.symToggles)
                        useSymbols = true
                    if (view.id == R.id.numbersToggle)
                        useNumbers = true
                    if (view.id == R.id.upperCaseToggle)
                        useUC = true
                    list.add(view.text.toString())
                }else{
                    if (view.id == R.id.lettersToggle)
                        useLetters = false
                    if (view.id == R.id.symToggles)
                        useSymbols = false
                    if (view.id == R.id.numbersToggle)
                        useNumbers = false
                    if (view.id == R.id.upperCaseToggle)
                        useUC = false
                    list.remove(view.text.toString())
                }
            }
        }

        lengthToggle.text = getString(R.string.length)  + ": " +  length
        lengthToggle.setOnClickListener {
            if(seekBar.visibility ==  View.GONE){
                seekBar.visibility =  View.VISIBLE
            }
            else{
                seekBar.visibility =  View.GONE
            }
        }

        // Set a SeekBar change listener
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                length = i
                lengthToggle.text = getString(R.string.length)  + ": " + length
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something
            }
        })

        generatePassword.setOnClickListener {
            val myPasswordManager = PasswordManager()
            //Create a password with letters, uppercase letters, numbers but not special chars with 17 chars
            if(list.size == 0 || (list.size == 1 && lengthToggle.isChecked)){
                genPasswordId.error = getString(R.string.noRules)
            }
            else {
                genPasswordId.error = null
                val newPassword: String =
                    myPasswordManager.generatePassword(useLetters, useUC, useNumbers, useSymbols, length)
                genPasswordIdField.setText(newPassword)
            }
            val deg = 0f
            generatePassword.animate().rotation(deg).interpolator = AccelerateDecelerateInterpolator()
        }
        generatePassword.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        cardPass.elevation = 50F
                        generatePassword.background = ContextCompat.getDrawable(this, R.color.grey)
                        v.invalidate()
                    }
                    MotionEvent.ACTION_UP -> {
                        generatePassword.background = ContextCompat.getDrawable(this, R.color.white)
                        cardPass.elevation = 10F
                        v.invalidate()
                    }
                }
                false
            }

        genPasswordId.setOnClickListener {
            if(genPasswordIdField.text.toString() != ""){
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Password", genPasswordIdField.text.toString())
                clipboard.setPrimaryClip(clip)
                toast(getString(R.string.passCopied))
            }
        }

        genPasswordIdField.setOnClickListener {
            if(genPasswordIdField.text.toString() != ""){
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Password", genPasswordIdField.text.toString())
                clipboard.setPrimaryClip(clip)
                toast(getString(R.string.passCopied))
            }
        }

        noPasswords.setOnClickListener {
            val intent = Intent(this, NewPasswordActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("pass", genPasswordIdField.text.toString())
            intent.putExtra("useLetters", useLetters)
            intent.putExtra("useUC", useUC)
            intent.putExtra("useNumbers", useNumbers)
            intent.putExtra("useSymbols", useSymbols)
            intent.putExtra("length", length)
            startActivity(intent)
            finish()
        }


        newPass.setOnClickListener {
            val intent = Intent(this, NewPasswordActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("pass", genPasswordIdField.text.toString())
            intent.putExtra("useLetters", useLetters)
            intent.putExtra("useUC", useUC)
            intent.putExtra("useNumbers", useNumbers)
            intent.putExtra("useSymbols", useSymbols)
            intent.putExtra("length", length)
            startActivity(intent)
            finish()
        }

        showAll.hide()

        showAll.setOnClickListener {
            showAll.hide()
            horizontalScroll.visibility = View.VISIBLE
            genPasswordId.visibility = View.VISIBLE
            cardPass.visibility = View.VISIBLE
            fixPasswords.visibility = View.VISIBLE
            correctPasswords.visibility = View.VISIBLE
            negativePasswords.visibility = View.VISIBLE
            positiveCircle.visibility = View.VISIBLE
            negativeCircle.visibility = View.VISIBLE
            negativeCircle2.visibility = View.VISIBLE
        }

        allPassword.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                if(searchPass.visibility == View.GONE) {
                    newPass.hide()
                    search.hide()
                }
                if(horizontalScroll.visibility == View.VISIBLE) {
                    allPassword.scrollTo(0, 0)
                    horizontalScroll.visibility = View.GONE
                    genPasswordId.visibility = View.GONE
                    cardPass.visibility = View.GONE
                    fixPasswords.visibility = View.GONE
                    correctPasswords.visibility = View.GONE
                    negativePasswords.visibility = View.GONE
                    positiveCircle.visibility = View.GONE
                    negativeCircle.visibility = View.GONE
                    negativeCircle2.visibility = View.GONE
                    showAll.show()
                }
            }
            else if(searchPass.visibility == View.GONE) {
                    newPass.show()
                    search.show()
            }
        })
    }

    private fun analyzeDataBase() {
        var subContains: Boolean
        var gSubContains: Boolean
        for (pass in realPass){
            subContains = false
            gSubContains = false
            for (pass2 in realPass){
                if(pass.first != pass2.first){
                    for(i in 0..(pass.second.length - 4)){
                        if (pass2.second.contains(pass.second.subSequence(i, i + 3))){
                            subContains = true
                            gSubContains = true
                            break
                        }
                    }
                    if (subContains)
                        if (realMap.containsKey(pass.first))
                            realMap[pass.first]?.add(pass2.first)
                        else {
                            val c = arrayListOf(pass2.first)
                            realMap[pass.first] = c
                        }
                        subContains = false
                }
            }
            if (gSubContains) {
                realQuality.add("0")
            }
            else
                realQuality.add("1")
        }
    }

    private fun passClickListener(position: Int) {
        val intent = Intent(this, PasswordViewActivity::class.java)
        var isPass = false
        intent.putExtra("login", login)
        intent.putExtra("passName", passwordsG[position].first)
        var str = getString(R.string.sameParts)
        if (realMap.containsKey(passwordsG[position].first)){
            for(pass in realMap[passwordsG[position].first]!!) {
                isPass = true
                str += "$pass "
            }
        }
        if(isPass)
            intent.putExtra("sameWith", str)
        else
            intent.putExtra("sameWith", "none")
        startActivity(intent)
        finish()
    }

    private fun Context.toast(message:String)=
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()

}
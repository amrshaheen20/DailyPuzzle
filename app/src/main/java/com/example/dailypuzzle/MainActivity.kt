package com.example.dailypuzzle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import kotlin.math.log

@Parcelize
data class Question(
    val Text: String,
    val Answer: String,
    val syllables: String
) : Parcelable

val IsSuccessKey = "IsSuccess"
var IsSuccess = false

val LastDateKey = "LastDate"
var LastDate = ""

val DateOfFirstRunKey = "DateOfFirstRun"
var DateOfFirstRun = ""


private fun readQuestionsFromRaw(context: Context): List<Question> {
    val inputStream = context.resources.openRawResource(R.raw.puzzles)
    val json = inputStream.bufferedReader().use { it.readText() }

    val type = object : TypeToken<List<Question>>() {}.type
    return Gson().fromJson(json, type)
}

fun getCurrentDate(): String {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    return "$year-$month-$day"
}

fun getPuzzleDays(): Int {
    val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")

    val startDateStr = LastDate;
    val endDateStr = getCurrentDate();
    if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
        return 1
    }

    val startDate = LocalDate.parse(startDateStr, formatter)
    val endDate = LocalDate.parse(endDateStr, formatter)

    return (ChronoUnit.DAYS.between(startDate, endDate) + 1).toInt()
}

fun SetValue(context: Context, key: String, value: Any) {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    when (value) {
        is String -> editor.putString(key, value)
        is Int -> editor.putInt(key, value)
        is Boolean -> editor.putBoolean(key, value)
        else -> throw IllegalArgumentException("Unsupported type")
    }

    editor.apply()
}

fun GetString(context: Context, key: String): String {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString(key, "") ?: ""
}

fun GetBoolean(context: Context, key: String): Boolean {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(key, false)
}

fun RemoveKey(context: Context, key: String) {
    val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.remove(key)
    editor.apply()
}


fun ResetApp(activity: Activity) {
    SetValue(activity, LastDateKey, "")

    Toast.makeText(activity, "تم مسح بيانات اللعبه بنجاح 😉", Toast.LENGTH_SHORT).show()
    val intent = Intent(activity, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    activity.startActivity(intent)

    activity.finish()
}


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        IsSuccess = GetBoolean(this, IsSuccessKey)
        LastDate = GetString(this, LastDateKey)
        DateOfFirstRun = GetString(this, DateOfFirstRunKey)

        if (DateOfFirstRun.isNullOrEmpty()) {
            DateOfFirstRun = getCurrentDate()
            SetValue(this, DateOfFirstRunKey, DateOfFirstRun)
        }

        val questions = readQuestionsFromRaw(this)
//        questions.forEach {
//            println("Text: ${it.Text}, Answer: ${it.Answer}, Syllables: ${it.syllables}")
//        }


        println("Today's date only: ${getCurrentDate()}")
        println("Puzzle days: ${getPuzzleDays()}")



        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, StartGame::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

            if (LastDate == getCurrentDate()) {
                if (IsSuccess) {
                    intent.setClass(this, success_screen::class.java)
                } else {
                    intent.setClass(this, fail_screen::class.java)
                }
            }

            intent.putExtra("question", questions.random())
            startActivity(intent)
            finish()

        }, 3000)





        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

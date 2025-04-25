package com.example.dailypuzzle

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.concurrent.thread

class StartGame : AppCompatActivity() {

    var currentIndex = 0
    var isGamePaused: Boolean = false
    var question: Question? = null
    var syllableButtons: Array<TextView> = arrayOf()
    var answerBlocks: Array<TextView> = arrayOf()
    var failNumberCounter = 3;
    //colors
    val FAIL_COLOR = R.color.fail_card
    val SUCCESS_COLOR = R.color.success_card


    private fun pauseGame() {
        isGamePaused = true
    }

    private fun resumeGame() {
        isGamePaused = false
    }

    private fun answerLenght(): Int {
        return question?.Answer?.length ?: 0
    }

    private fun calcCurrentIndex() {
        currentIndex = 0
        for (button in answerBlocks) {
            if (button.text.isNotEmpty()) {
                currentIndex++
            } else {
                break
            }
        }
    }

    private fun flashAnswerBlocks(colorResId: Int) {
        pauseGame()

        val originalTints = answerBlocks.map { it.backgroundTintList }
        val targetTint = ColorStateList.valueOf(getColor(colorResId))

        answerBlocks.forEach { it.backgroundTintList = targetTint }

        Handler(Looper.getMainLooper()).postDelayed({
            Thread.sleep(500)
            for (i in answerBlocks.indices) {
                answerBlocks[i].backgroundTintList = originalTints[i]
            }
            resumeGame()
        }, 500)
    }


    private fun isCorrectAnswer(): Boolean {

        var answer = ""
        for (button in answerBlocks) {
            answer += button.text
        }
        return answer.equals(question?.Answer)
    }

    private fun updateaAttemptsText() {
        findViewById<TextView>(R.id.attemptsText).text =
            "عدد المحاولات المتبقية: $failNumberCounter"
    }

    private fun isGameOver(): Boolean {
        return failNumberCounter == 0;
    }

    private fun checkGameAnswer() {
        if (currentIndex == answerLenght()) {
            if (isCorrectAnswer()) {
                flashAnswerBlocks(SUCCESS_COLOR)
                SetValue(this, IsSuccessKey, true)
                SetValue(this, LastDateKey, getCurrentDate())

                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, success_screen::class.java)
                    startActivity(intent)
                    finish()
                }, 800)
                return
            }

            flashAnswerBlocks(FAIL_COLOR)
            failNumberCounter--

            if (isGameOver()) {
                SetValue(this, IsSuccessKey, false)
                SetValue(this, LastDateKey, getCurrentDate())

                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, fail_screen::class.java)
                    startActivity(intent)
                    finish()
                }, 800)
                return
            }

            updateaAttemptsText()


        }
    }

    fun getArabicOrder(number: Int): String {
        return when (number) {
            1 -> "الأول"
            2 -> "الثاني"
            3 -> "الثالث"
            4 -> "الرابع"
            5 -> "الخامس"
            6 -> "السادس"
            7 -> "السابع"
            8 -> "الثامن"
            9 -> "التاسع"
            10 -> "العاشر"
            11 -> "الحادي عشر"
            12 -> "الثاني عشر"
            13 -> "الثالث عشر"
            14 -> "الرابع عشر"
            15 -> "الخامس عشر"
            16 -> "السادس عشر"
            17 -> "السابع عشر"
            18 -> "الثامن عشر"
            19 -> "التاسع عشر"
            20 -> "العشرون"
            else -> "رقم $number"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_game)
        updateaAttemptsText()

        question = intent.getParcelableExtra<Question>("question")
        if (question == null) {
            return;
        }

        findViewById<TextView>(R.id.question).text = question?.Text

        findViewById<TextView>(R.id.puzzleName).text = "لغز اليوم ${getArabicOrder(getPuzzleDays())}"





        syllableButtons = arrayOf(
            findViewById<TextView>(R.id.syllableButton1),
            findViewById<TextView>(R.id.syllableButton2),
            findViewById<TextView>(R.id.syllableButton3),
            findViewById<TextView>(R.id.syllableButton4),
            findViewById<TextView>(R.id.syllableButton5),
            findViewById<TextView>(R.id.syllableButton6),
            findViewById<TextView>(R.id.syllableButton7),
            findViewById<TextView>(R.id.syllableButton8),
            findViewById<TextView>(R.id.syllableButton9),
            findViewById<TextView>(R.id.syllableButton10),
            findViewById<TextView>(R.id.syllableButton11),
            findViewById<TextView>(R.id.syllableButton12),
        )

        answerBlocks = arrayOf(
            findViewById<TextView>(R.id.answerBlock1),
            findViewById<TextView>(R.id.answerBlock2),
            findViewById<TextView>(R.id.answerBlock3),
            findViewById<TextView>(R.id.answerBlock4),
            findViewById<TextView>(R.id.answerBlock5),
            findViewById<TextView>(R.id.answerBlock6),
        )


        var index = 0
        question?.syllables?.forEach { answerChar ->
            if (index < syllableButtons.size) {
                val button = syllableButtons[index]
                button.text = answerChar.toString()
                val indextotag = index.toInt()
                button.setOnClickListener {

                    if (currentIndex < answerLenght() && !isGamePaused) {
                        answerBlocks[currentIndex].text = answerChar.toString()

                        answerBlocks[currentIndex].tag = indextotag
                        button.visibility = View.INVISIBLE
                        calcCurrentIndex()

                        checkGameAnswer()
                    }
                }
                index++
            }
        }


        for (i in answerBlocks.indices) {
            answerBlocks[i].visibility =
                if (i < (question?.Answer?.length ?: 0)) View.VISIBLE else View.GONE
            answerBlocks[i].text = ""
            answerBlocks[i].setOnClickListener {
                val tag = answerBlocks[i].tag
                if (tag is Int && !isGamePaused) {
                    syllableButtons[tag].visibility = View.VISIBLE
                    answerBlocks[i].text = ""
                    answerBlocks[i].tag = null
                    calcCurrentIndex()
                }
            }

        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
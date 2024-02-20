package com.deneme.yapyoruz

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.BufferedReader
import kotlin.random.Random
import kotlin.random.nextInt

class MainActivity : AppCompatActivity() {

    private var currentFlagIndex = 0
    private var countries = mutableListOf<String>()
    private var dataList = mutableListOf<List<String>>() // Define dataList at the class level
    private var score = 0
    private var capitals = mutableListOf<String>()
    private var questionNumber = 0 // To track the question number
    private var totalQuestions = 10 // Total number of questions
    private var correctAnswersInFirstAttempt = 0 // Number of questions answered correctly on the first attempt
    private var bonusQuestionAnswered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val flag: ImageView = findViewById(R.id.imageView)
        val option1: Button = findViewById(R.id.button1)
        val option2: Button = findViewById(R.id.button2)
        val option3: Button = findViewById(R.id.button3)

        BufferedReader(assets.open("flags.csv").reader()).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                val data = line.split(",")
                dataList.add(data)
                line = reader.readLine()
            }
        }

        // Create the countries list
        for (i in 0 until dataList.size) {
            countries.add(dataList[i][3])
            capitals.add(dataList[i][4])
        }

        // Display the question
        displayRandomQuestion(flag, option1, option2, option3)

        option1.setOnClickListener {
            checkAnswer(flag, option1.text.toString(), option1, option2, option3)
        }
        option2.setOnClickListener {
            checkAnswer(flag, option2.text.toString(), option1, option2, option3)
        }
        option3.setOnClickListener {
            checkAnswer(flag, option3.text.toString(), option1, option2, option3)
        }
    }

    private fun displayRandomQuestion(flag: ImageView, option1: Button, option2: Button, option3: Button) {

        if (questionNumber < totalQuestions) {
            // Select a random flag
            currentFlagIndex = Random.nextInt(0, countries.size)
            val url = dataList[currentFlagIndex][0]
            val questionNumberTextView: TextView = findViewById(R.id.textView)

            // Show the flag
            Glide.with(this)
                .load(url)
                .into(flag)

            // Get the correct answer
            val correctCountry = countries[currentFlagIndex]

            // Get the wrong answers
            val wrongOptions = mutableListOf<String>()
            while (wrongOptions.size < 2) {
                val random = Random.nextInt(0, countries.size)
                val wrongOption = countries[random]
                if (wrongOption != correctCountry && !wrongOptions.contains(wrongOption)) {
                    wrongOptions.add(wrongOption)
                }
            }

            // Shuffle the wrong answers and the correct answer
            val allOptions = mutableListOf<String>().apply {
                add(correctCountry)
                addAll(wrongOptions)
            }.shuffled()

            // Place the answers on the buttons
            option1.text = allOptions[0]
            option2.text = allOptions[1]
            option3.text = allOptions[2]

            // Update the question number
            updateQuestionNumber()

            // Show the question number on the screen
            val questionNumberText = "Question $questionNumber of $totalQuestions"
            questionNumberTextView.text = questionNumberText
        } else {
            // Show the result screen after all questions have been asked

            if (bonusQuestionAnswered) {
                showResultScreen()
                bonusQuestionAnswered = false // Reset the flag after displaying the next question
            }
        }
    }


    private var attemptsRemaining = 3

    private fun checkAnswer(flag: ImageView, selectedOption: String, option1: Button, option2: Button, option3: Button) {
        val correctCountry = countries[currentFlagIndex]
        updateAnswerStatus("")
        if (selectedOption == correctCountry) {
            when (attemptsRemaining) {
                3 -> {
                    score += 20
                    updateAnswerStatus("Correct")
                    attemptsRemaining = 3
                    correctAnswersInFirstAttempt++
                    askBonusQuestion()
                    if (bonusQuestionAnswered) {
                        displayRandomQuestion(flag, option1, option2, option3)
                        bonusQuestionAnswered = false // Reset the flag after displaying the next question
                    }
                }
                2 -> {
                    score += 10
                    updateAnswerStatus("Correct")
                    attemptsRemaining = 3
                    displayRandomQuestion(flag, option1, option2, option3)
                }
                1 -> {
                    updateAnswerStatus("Correct")
                    attemptsRemaining = 3
                    displayRandomQuestion(flag, option1, option2, option3)
                }
            }

        } else {
            if (attemptsRemaining > 1) {
                // If there are still attempts left, allow the user to try again
                updateAnswerStatus("Wrong. Try again!")
                attemptsRemaining--
            } else {
                // No more attempts left
                updateAnswerStatus("Wrong. The correct answer is $correctCountry.")
                displayRandomQuestion(flag, option1, option2, option3)
                // Reset the attempt count for the next question
                attemptsRemaining = 3
            }
        }
    }


    private fun updateAnswerStatus(status: String) {
        // Write the status to TextView3
        val answerStatusTextView: TextView = findViewById(R.id.textView3)
        answerStatusTextView.text = status
    }

    private fun askBonusQuestion() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Bonus Question")
        builder.setMessage("What is the capital of this country?")

        // Get the country's capital as the correct answer
        val correctCapital = getCapitalOfCurrentCountry(currentFlagIndex)

        // Get random capitals
        val wrongCapital1 = randomCapital()
        val wrongCapital2 = randomCapital()

        // Shuffle the options
        val options = listOf(correctCapital, wrongCapital1, wrongCapital2).shuffled()

        builder.setPositiveButton(options[0]) { dialog, which ->
            // Actions to take when the correct capital is selected
            score += 5
            Toast.makeText(this, correctCapital + " is the capital.", Toast.LENGTH_SHORT).show()
            bonusQuestionAnswered = true // Set the flag when the user answers the bonus question
        }

        builder.setNegativeButton(options[1]) { dialog, which ->
            Toast.makeText(this, correctCapital + " is the capital.", Toast.LENGTH_SHORT).show()
            bonusQuestionAnswered = true // Set the flag when the user answers the bonus question
        }

        builder.setNeutralButton(options[2]) { dialog, which ->
            Toast.makeText(this, correctCapital + " is the capital.", Toast.LENGTH_SHORT).show()
            bonusQuestionAnswered = true // Set the flag when the user answers the bonus question
        }

        builder.create().show()
    }

    private fun getCapitalOfCurrentCountry(currentFlagIndex: Int): String {
        return dataList[currentFlagIndex][4]
    }

    private fun randomCapital(): String {
        val random = Random.nextInt(0..205)
        return dataList[random][4]
    }

    private fun updateQuestionNumber() {
        questionNumber++
    }

    private fun showResultScreen() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Result")
        builder.setMessage("Total Score: $score\nCorrect Answers on First Attempt: $correctAnswersInFirstAttempt")

        builder.setPositiveButton("Okay") { dialog, which ->
            // Actions to take when the user presses Okay
            // For example, restarting the quiz
            restartQuiz()
        }

        builder.create().show()
    }

    private fun restartQuiz() {
        // Reset all relevant variables and start the quiz again
        currentFlagIndex = 0
        score = 0
        correctAnswersInFirstAttempt = 0
        bonusQuestionAnswered = false
        questionNumber = 0
        attemptsRemaining = 3

        // Find the views
        val flag: ImageView = findViewById(R.id.imageView)
        val option1: Button = findViewById(R.id.button1)
        val option2: Button = findViewById(R.id.button2)
        val option3: Button = findViewById(R.id.button3)

        // Display the first question
        displayRandomQuestion(flag, option1, option2, option3)
    }

}

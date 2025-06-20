package com.bignerdranch.android.geomain

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

private const val TAG = "MainActivity"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {

    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var nextButton: Button
    private lateinit var cheatButton: Button
    private lateinit var questionTextView: TextView

    companion object {
        private const val KEY_CURRENT_INDEX = "currentIndex"
        private const val KEY_CORRECT_ANSWERS = "correctAnswers"
    }

    private val quizViewModel: QuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        setContentView(R.layout.activity_main)

        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        cheatButton = findViewById(R.id.cheat_button)
        questionTextView = findViewById(R.id.question_text_view)

        if (savedInstanceState != null) {
            quizViewModel.currentIndex = savedInstanceState.getInt(KEY_CURRENT_INDEX, 0)
            quizViewModel.correctAnswers = savedInstanceState.getInt(KEY_CORRECT_ANSWERS, 0)
        }

        trueButton.setOnClickListener {
            checkAnswer(true)
            hideAnswerButtons()
        }

        falseButton.setOnClickListener {
            checkAnswer(false)
            hideAnswerButtons()
        }

        nextButton.setOnClickListener {
            quizViewModel.moveToNext()
            updateQuestion()
            showAnswerButtons()
        }

        cheatButton.setOnClickListener {
            if (quizViewModel.cheatsRemaining > 0) {
                val answerIsTrue = quizViewModel.currentQuestionAnswer
                val intent = CheatActivity.newIntent(
                    this@MainActivity,
                    answerIsTrue,
                    quizViewModel.cheatsRemaining
                )
                startActivityForResult(intent, REQUEST_CODE_CHEAT)
            } else {
                Toast.makeText(this, R.string.no_more_cheats_toast, Toast.LENGTH_SHORT).show()
                quizViewModel.isCheater = true
            }
        }

        updateQuestion()
    }

    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int,
                                  data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_CODE_CHEAT) {
            val answerWasShown = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
            if (answerWasShown) {
                quizViewModel.useCheat()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_INDEX, quizViewModel.currentIndex)
        outState.putInt(KEY_CORRECT_ANSWERS, quizViewModel.correctAnswers)
    }

    private fun updateQuestion() {
        questionTextView.setText(quizViewModel.currentQuestionText)

        if (quizViewModel.currentQuestion.isAnswered) {
            hideAnswerButtons()
        } else {
            showAnswerButtons()
        }

        if (quizViewModel.currentIndex < quizViewModel.questionBank.size - 1) {
            enableNextButton()
        } else {
            if (!quizViewModel.currentQuestion.isAnswered) {
                enableNextButton()
            } else {
                disableNextButton()
            }
        }
    }

    private fun checkAnswer(userAnswer: Boolean) {
        quizViewModel.markQuestionAsAnswered()

        val isCorrect = userAnswer == quizViewModel.currentQuestionAnswer
        val messageResId = when {
            quizViewModel.isCurrentQuestionCheated() -> R.string.judgment_toast
            isCorrect -> {
                quizViewModel.correctAnswers++
                R.string.correct_toast
            }
            else -> R.string.incorrect_toast
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
        hideAnswerButtons()

        if (quizViewModel.currentIndex == quizViewModel.questionBank.size - 1) {
            disableNextButton()
            showResult()
        }
    }

    private fun showResult() {
        val totalQuestions = quizViewModel.questionBank.size
        val resultMessage = getString(
            R.string.result_message,
            quizViewModel.correctAnswers,
            totalQuestions
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.results_title)
            .setMessage(resultMessage)
            .setPositiveButton(R.string.restart_quiz) { _, _ ->
                restartQuiz()
            }
            .setCancelable(false)
            .show()
    }

    private fun restartQuiz() {
        quizViewModel.resetQuiz()
        updateQuestion()
        showAnswerButtons()
        enableNextButton()
    }

    private fun disableNextButton() {
        nextButton.isEnabled = false
        nextButton.visibility = View.INVISIBLE
    }

    private fun enableNextButton() {
        nextButton.isEnabled = true
        nextButton.visibility = View.VISIBLE
    }

    private fun hideAnswerButtons() {
        trueButton.visibility = View.INVISIBLE
        falseButton.visibility = View.INVISIBLE
    }

    private fun showAnswerButtons() {
        trueButton.visibility = View.VISIBLE
        falseButton.visibility = View.VISIBLE
    }
}
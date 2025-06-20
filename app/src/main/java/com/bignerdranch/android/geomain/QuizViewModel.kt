package com.bignerdranch.android.geomain

import androidx.lifecycle.ViewModel

private const val TAG = "QuizViewModel"

class QuizViewModel : ViewModel() {
    var currentIndex = 0
    var correctAnswers = 0
    var isCheater = false
    var cheatsRemaining = 3
    private var currentQuestionCheated = false

    val currentQuestion: Question
        get() = questionBank[currentIndex]

    val questionBank = listOf(
        Question(R.string.question_australia, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_mideast, false),
        Question(R.string.question_africa, false),
        Question(R.string.question_americas, true),
        Question(R.string.question_asia, true)
    )

    val currentQuestionAnswer: Boolean
        get() = questionBank[currentIndex].answer
    val currentQuestionText: Int
        get() = questionBank[currentIndex].textResId
    fun isCurrentQuestionCheated(): Boolean {
        return currentQuestionCheated
    }
    fun moveToNext() {
        currentIndex = (currentIndex + 1) % questionBank.size
        currentQuestionCheated = false
    }
    fun markQuestionAsAnswered() {
        currentQuestion.isAnswered = true
    }
    fun useCheat() {
        if (cheatsRemaining > 0) {
            cheatsRemaining--
            currentQuestionCheated = true
        }
    }
    fun resetQuiz() {
        currentIndex = 0
        correctAnswers = 0
        isCheater = false
        cheatsRemaining = 3
        questionBank.forEach { it.isAnswered = false }
        currentQuestionCheated = false
    }
}
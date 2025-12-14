package com.gumkid.flashcard.model

import java.util.Date

data class Flashcard(
    val id: String = "",
    val question: String = "",
    val answer: String = "",
    val category: String = "General", // Default category
    val difficulty: Int = 3, // Default medium difficulty
    val lastReviewed: Date? = null,
    val createdAt: Date = Date(),
    val userId: String = "" // Ini akan diisi oleh repository
) {
    companion object {
        const val COLLECTION_NAME = "flashcards"
    }
}
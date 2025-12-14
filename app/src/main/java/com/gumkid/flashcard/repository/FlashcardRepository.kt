package com.gumkid.flashcard.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gumkid.flashcard.model.Flashcard
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class FlashcardRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    suspend fun getAllFlashcards(): List<Flashcard> {
        return try {
            val currentUser = auth.currentUser ?: return emptyList()
            val querySnapshot = firestore.collection(Flashcard.COLLECTION_NAME)
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            querySnapshot.documents.map { document ->
                document.toObject(Flashcard::class.java)?.copy(id = document.id) ?: throw Exception("Invalid document")
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFlashcardById(id: String): Flashcard? {
        return try {
            val currentUser = auth.currentUser ?: return null
            val document = firestore.collection(Flashcard.COLLECTION_NAME)
                .document(id)
                .get()
                .await()

            if (document.exists()) {
                val flashcard = document.toObject(Flashcard::class.java)?.copy(id = document.id)
                if (flashcard?.userId == currentUser.uid) flashcard else null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addFlashcard(flashcard: Flashcard): String {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            val newFlashcard = flashcard.copy(
                userId = currentUser.uid,
                createdAt = Date()
            )

            val document = firestore.collection(Flashcard.COLLECTION_NAME)
                .add(newFlashcard)
                .await()

            document.id
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateFlashcard(flashcard: Flashcard): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            if (flashcard.userId != currentUser.uid) return false

            val updateData = mapOf(
                "question" to flashcard.question,
                "answer" to flashcard.answer,
                "category" to flashcard.category,
                "difficulty" to flashcard.difficulty,
                "lastReviewed" to Date()
            )

            firestore.collection(Flashcard.COLLECTION_NAME)
                .document(flashcard.id)
                .update(updateData)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteFlashcard(id: String): Boolean {
        return try {
            firestore.collection(Flashcard.COLLECTION_NAME)
                .document(id)
                .delete()
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getFlashcardsByCategory(category: String): List<Flashcard> {
        return try {
            val currentUser = auth.currentUser ?: return emptyList()
            val querySnapshot = firestore.collection(Flashcard.COLLECTION_NAME)
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("category", category)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.map { document ->
                document.toObject(Flashcard::class.java)?.copy(id = document.id) ?: throw Exception("Invalid document")
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
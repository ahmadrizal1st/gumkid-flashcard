package com.gumkid.flashcard.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gumkid.flashcard.model.Flashcard
import com.gumkid.flashcard.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val repository: FlashcardRepository
) : ViewModel() {

    private val _flashcards = MutableLiveData<List<Flashcard>>()
    val flashcards: LiveData<List<Flashcard>> = _flashcards

    private val _currentFlashcard = MutableLiveData<Flashcard?>()
    val currentFlashcard: LiveData<Flashcard?> = _currentFlashcard

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    init {
        loadAllFlashcards()
    }

    fun loadAllFlashcards() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getAllFlashcards()
                _flashcards.value = result
                extractCategories(result)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load flashcards: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFlashcardById(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val flashcard = repository.getFlashcardById(id)
                _currentFlashcard.value = flashcard
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load flashcard: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFlashcard(question: String, answer: String, category: String, difficulty: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val flashcard = Flashcard(
                    question = question,
                    answer = answer,
                    category = category,
                    difficulty = difficulty
                )

                repository.addFlashcard(flashcard)
                loadAllFlashcards()
                _successMessage.value = "Flashcard added successfully"
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add flashcard: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFlashcard(
        id: String,
        question: String,
        answer: String,
        category: String,
        difficulty: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val existingFlashcard = repository.getFlashcardById(id)
                if (existingFlashcard == null) {
                    _errorMessage.value = "Flashcard not found"
                    return@launch
                }

                val flashcard = existingFlashcard.copy(
                    question = question,
                    answer = answer,
                    category = category,
                    difficulty = difficulty
                )

                repository.updateFlashcard(flashcard)
                loadAllFlashcards()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update flashcard: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFlashcard(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteFlashcard(id)
                loadAllFlashcards()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete flashcard: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFlashcardsByCategory(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getFlashcardsByCategory(category)
                _flashcards.value = result
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load flashcards: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun extractCategories(flashcards: List<Flashcard>) {
        val categorySet = mutableSetOf<String>()
        flashcards.forEach { flashcard ->
            if (flashcard.category.isNotEmpty()) {
                categorySet.add(flashcard.category)
            }
        }
        _categories.value = categorySet.toList()
    }

    fun clearCurrentFlashcard() {
        _currentFlashcard.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
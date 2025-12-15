package com.gumkid.flashcard.viewmodel

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gumkid.flashcard.model.Flashcard
import com.gumkid.flashcard.repository.FlashcardRepository
import com.gumkid.flashcard.ui.flashcardlist.FilterDialogFragment
import com.gumkid.flashcard.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val repository: FlashcardRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _allFlashcards = MutableLiveData<List<Flashcard>>()
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

    private val _totalCount = MutableLiveData<Int>()
    val totalCount: LiveData<Int> = _totalCount

    private val _reviewCount = MutableLiveData<Int>()
    val reviewCount: LiveData<Int> = _reviewCount

    // Current filter state
    private var currentSearchQuery: String? = null
    private var currentSelectedCategories: List<String>? = null
    private var currentSelectedDifficulties: List<Int>? = null

    init {
        loadAllFlashcards()
    }

    fun loadAllFlashcards() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getAllFlashcards()
                _allFlashcards.value = result
                extractCategories(result)
                _totalCount.value = result.size
                _reviewCount.value = result.size // For now, assume all need review
                applyFilters()
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
                NotificationHelper.showFlashcardNotification(
                    context,
                    "Flashcard Added",
                    "New flashcard has been added successfully"
                )
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
                _successMessage.value = "Flashcard updated successfully"
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
        filterByCategories(listOf(category))
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

    private fun applyFilters() {
        val allFlashcards = _allFlashcards.value ?: return
        var filtered = allFlashcards

        // Apply search query
        currentSearchQuery?.let { query ->
            if (query.isNotBlank()) {
                filtered = filtered.filter { flashcard ->
                    flashcard.question.contains(query, ignoreCase = true) ||
                    flashcard.answer.contains(query, ignoreCase = true) ||
                    flashcard.category.contains(query, ignoreCase = true)
                }
            }
        }

        // Apply category filter
        currentSelectedCategories?.let { categories ->
            if (categories.isNotEmpty()) {
                filtered = filtered.filter { flashcard ->
                    categories.any { it.equals(flashcard.category, ignoreCase = true) }
                }
            }
        }

        // Apply difficulty filter
        currentSelectedDifficulties?.let { difficulties ->
            if (difficulties.isNotEmpty()) {
                filtered = filtered.filter { flashcard ->
                    difficulties.contains(flashcard.difficulty)
                }
            }
        }

        _flashcards.value = filtered
    }

    fun searchFlashcards(query: String?) {
        currentSearchQuery = query
        applyFilters()
    }

    fun filterByCategories(categories: List<String>?) {
        currentSelectedCategories = categories
        applyFilters()
    }

    fun filterByDifficulties(difficulties: List<Int>?) {
        currentSelectedDifficulties = difficulties
        applyFilters()
    }

    fun clearFilters() {
        currentSearchQuery = null
        currentSelectedCategories = null
        currentSelectedDifficulties = null
        applyFilters()
    }

    fun getCurrentSelectedCategories(): List<String>? = currentSelectedCategories

    fun getCurrentSelectedDifficulties(): List<Int>? = currentSelectedDifficulties

    fun showFilterDialog(fragment: androidx.fragment.app.Fragment) {
        val filterDialog = FilterDialogFragment()
        filterDialog.show(fragment.childFragmentManager, "FilterDialog")
    }
}

package com.gumkid.flashcard.ui.flashcardlist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.gumkid.flashcard.R
import com.gumkid.flashcard.databinding.DialogFilterBinding
import com.gumkid.flashcard.viewmodel.FlashcardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilterDialogFragment : DialogFragment() {

    private var _binding: DialogFilterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlashcardViewModel by viewModels()

    private val selectedCategories = mutableListOf<String>()
    private val selectedDifficulties = mutableListOf<Int>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_filter)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize selected lists with current filter state (store in lowercase for case-insensitive comparison)
        selectedCategories.addAll(viewModel.getCurrentSelectedCategories()?.map { it.lowercase() } ?: emptyList())
        selectedDifficulties.addAll(viewModel.getCurrentSelectedDifficulties() ?: emptyList())

        setupCategoryCheckboxes()
        setupDifficultyCheckboxes()
        setupListeners()
        updateApplyButtonState()
    }

    private fun setupCategoryCheckboxes() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            binding.categoriesContainer.removeAllViews()

            // Remove categories that are no longer available (case-insensitive)
            selectedCategories.retainAll(categories.map { it.lowercase() })

            categories.forEach { category ->
                val checkBox = CheckBox(requireContext()).apply {
                    text = category
                    isChecked = selectedCategories.contains(category)
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedCategories.add(category)
                        } else {
                            selectedCategories.remove(category)
                        }
                        updateApplyButtonState()
                    }
                }
                binding.categoriesContainer.addView(checkBox)
            }
        }
    }

    private fun setupDifficultyCheckboxes() {
        val difficultyCheckboxes = listOf(
            binding.cbDifficulty1 to 1,
            binding.cbDifficulty2 to 2,
            binding.cbDifficulty3 to 3,
            binding.cbDifficulty4 to 4,
            binding.cbDifficulty5 to 5
        )

        difficultyCheckboxes.forEach { (checkBox, difficulty) ->
            checkBox.isChecked = selectedDifficulties.contains(difficulty)
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedDifficulties.add(difficulty)
                } else {
                    selectedDifficulties.remove(difficulty)
                }
                updateApplyButtonState()
            }
        }
    }

    private fun setupListeners() {
        binding.btnApply.setOnClickListener {
            applyFilters()
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnClearFilters.setOnClickListener {
            clearFilters()
            dismiss()
        }
    }

    private fun applyFilters() {
        // Map lowercase selected categories back to original case
        val originalCaseCategories = if (selectedCategories.isNotEmpty()) {
            viewModel.categories.value?.filter { category ->
                selectedCategories.contains(category.lowercase())
            } ?: emptyList()
        } else {
            null
        }
        viewModel.filterByCategories(originalCaseCategories)
        viewModel.filterByDifficulties(selectedDifficulties.ifEmpty { null })
    }

    private fun clearFilters() {
        viewModel.clearFilters()
    }

    private fun updateApplyButtonState() {
        binding.btnApply.isEnabled = selectedCategories.isNotEmpty() || selectedDifficulties.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.gumkid.flashcard.ui.addflashcard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gumkid.flashcard.databinding.FragmentAddFlashcardBinding
import com.gumkid.flashcard.viewmodel.FlashcardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFlashcardFragment : Fragment() {

    private var _binding: FragmentAddFlashcardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlashcardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFlashcardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupObservers()
        setupCategoryAdapter()

        // Setup slider listener
        binding.sDifficulty.addOnChangeListener { _, value, _ ->
            binding.tvDifficultyValue.text = value.toInt().toString()
        }
    }

    private fun setupCategoryAdapter() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val categoryAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories
            )
            binding.actCategory.setAdapter(categoryAdapter)
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { successMessage ->
            successMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
                viewModel.clearSuccessMessage()
            }
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val question = binding.etQuestion.text.toString().trim()
            val answer = binding.etAnswer.text.toString().trim()
            val category = binding.actCategory.text.toString().trim()
            val difficulty = binding.sDifficulty.value.toInt()

            if (validateInput(question, answer, category)) {
                viewModel.addFlashcard(question, answer, category, difficulty)
            }
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun validateInput(question: String, answer: String, category: String): Boolean {
        var isValid = true

        if (question.isEmpty()) {
            binding.tilQuestion.error = "Question is required"
            isValid = false
        } else {
            binding.tilQuestion.error = null
        }

        if (answer.isEmpty()) {
            binding.tilAnswer.error = "Answer is required"
            isValid = false
        } else {
            binding.tilAnswer.error = null
        }

        if (category.isEmpty()) {
            binding.tilCategory.error = "Category is required"
            isValid = false
        } else {
            binding.tilCategory.error = null
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
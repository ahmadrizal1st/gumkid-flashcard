package com.gumkid.flashcard.ui.editflashcard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gumkid.flashcard.R
import com.gumkid.flashcard.databinding.FragmentEditFlashcardBinding
import com.gumkid.flashcard.viewmodel.FlashcardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditFlashcardFragment : Fragment() {

    private var _binding: FragmentEditFlashcardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlashcardViewModel by viewModels()
    private val args: EditFlashcardFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditFlashcardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFlashcard()
        setupObservers()
        setupListeners()
        setupCategoryAdapter()
    }

    private fun loadFlashcard() {
        // Get flashcardId from navigation arguments
        val flashcardId = args.flashcardId
        viewModel.loadFlashcardById(flashcardId)
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
        viewModel.currentFlashcard.observe(viewLifecycleOwner) { flashcard ->
            flashcard?.let {
                binding.etQuestion.setText(it.question)
                binding.etAnswer.setText(it.answer)
                binding.actCategory.setText(it.category)
                binding.sDifficulty.value = it.difficulty.toFloat()

                // Update difficulty text
                binding.tvDifficultyValue.text = it.difficulty.toString()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccessMessage()
                // Navigate back to detail fragment
                findNavController().popBackStack()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }

        // Update difficulty text when slider changes
        binding.sDifficulty.addOnChangeListener { _, value, _ ->
            binding.tvDifficultyValue.text = value.toInt().toString()
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val question = binding.etQuestion.text.toString().trim()
            val answer = binding.etAnswer.text.toString().trim()
            val category = binding.actCategory.text.toString().trim()
            val difficulty = binding.sDifficulty.value.toInt()

            if (validateInput(question, answer, category)) {
                viewModel.currentFlashcard.value?.let { flashcard ->
                    viewModel.updateFlashcard(
                        flashcard.id,
                        question,
                        answer,
                        category,
                        difficulty
                    )
                }
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
        viewModel.clearCurrentFlashcard()
    }
}
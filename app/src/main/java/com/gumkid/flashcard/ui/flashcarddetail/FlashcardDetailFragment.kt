package com.gumkid.flashcard.ui.flashcarddetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gumkid.flashcard.databinding.FragmentFlashcardDetailBinding
import com.gumkid.flashcard.viewmodel.FlashcardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FlashcardDetailFragment : Fragment() {

    private var _binding: FragmentFlashcardDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlashcardViewModel by viewModels()
    private val args: FlashcardDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlashcardDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFlashcard()
        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // Reload flashcard data ketika kembali dari edit
        loadFlashcard()
    }

    private fun loadFlashcard() {
        // Get flashcardId from navigation arguments
        val flashcardId = args.flashcardId
        viewModel.loadFlashcardById(flashcardId)
    }

    private fun setupObservers() {
        viewModel.currentFlashcard.observe(viewLifecycleOwner) { flashcard ->
            flashcard?.let {
                binding.tvQuestion.text = it.question
                binding.tvAnswer.text = it.answer
                binding.tvCategory.text = "Category: ${it.category}"
                binding.tvDifficulty.text = "Difficulty: ${it.difficulty}/5"

                binding.tvAnswer.visibility = View.GONE
                binding.btnShowAnswer.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnShowAnswer.setOnClickListener {
            binding.tvAnswer.visibility = View.VISIBLE
            binding.btnShowAnswer.visibility = View.GONE
        }

        binding.btnEdit.setOnClickListener {
            viewModel.currentFlashcard.value?.let { flashcard ->
                // Navigate to edit fragment with flashcard ID
                val action = FlashcardDetailFragmentDirections
                    .actionFlashcardDetailFragmentToEditFlashcardFragment(flashcard.id)
                findNavController().navigate(action)
            }
        }

        binding.btnDelete.setOnClickListener {
            viewModel.currentFlashcard.value?.let { flashcard ->
                viewModel.deleteFlashcard(flashcard.id)
                findNavController().popBackStack()
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.clearCurrentFlashcard()
    }
}
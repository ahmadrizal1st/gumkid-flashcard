package com.gumkid.flashcard.ui.flashcardlist

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gumkid.flashcard.R
import com.gumkid.flashcard.adapter.FlashcardAdapter
import com.gumkid.flashcard.databinding.FragmentFlashcardListBinding
import com.gumkid.flashcard.viewmodel.AuthViewModel
import com.gumkid.flashcard.viewmodel.FlashcardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FlashcardListFragment : Fragment() {

    private var _binding: FragmentFlashcardListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlashcardViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var adapter: FlashcardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlashcardListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()
        loadFlashcards()
    }

    override fun onResume() {
        super.onResume()
        loadFlashcards()
    }

    private fun setupRecyclerView() {
        adapter = FlashcardAdapter(
            onItemClick = { flashcard ->
                // Navigate to detail with flashcard ID
                val action = FlashcardListFragmentDirections
                    .actionFlashcardListFragmentToFlashcardDetailFragment(flashcard.id)
                findNavController().navigate(action)
            },
            onViewClick = { flashcard ->
                // Navigate to detail with flashcard ID
                val action = FlashcardListFragmentDirections
                    .actionFlashcardListFragmentToFlashcardDetailFragment(flashcard.id)
                findNavController().navigate(action)
            },
            onEditClick = { flashcard ->
                // Navigate to edit with flashcard ID
                val action = FlashcardListFragmentDirections
                    .actionFlashcardListFragmentToEditFlashcardFragment(flashcard.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { flashcard ->
                viewModel.deleteFlashcard(flashcard.id)
            }
        )

        binding.recyclerViewFlashcards.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFlashcards.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.flashcards.observe(viewLifecycleOwner) { flashcards ->
            adapter.submitList(flashcards)
            binding.tvEmpty.visibility = if (flashcards.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.totalCount.observe(viewLifecycleOwner) { count ->
            binding.tvTotalCount.text = count.toString()
        }

        viewModel.reviewCount.observe(viewLifecycleOwner) { count ->
            binding.tvReviewCount.text = count.toString()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            // Navigate to add flashcard fragment
            findNavController().navigate(R.id.action_flashcardListFragment_to_addFlashcardFragment)
        }

        binding.btnRefresh.setOnClickListener {
            loadFlashcards()
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.signOut()
            findNavController().navigate(R.id.action_flashcardListFragment_to_loginFragment)
        }

        // Search functionality
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchFlashcards(s?.toString())
            }
        })

        // Filter button - show filter dialog
        binding.btnFilter.setOnClickListener {
            val filterDialog = FilterDialogFragment()
            filterDialog.show(childFragmentManager, "FilterDialog")
        }
    }

    private fun loadFlashcards() {
        viewModel.loadAllFlashcards()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
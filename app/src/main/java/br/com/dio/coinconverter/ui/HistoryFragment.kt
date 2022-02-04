package br.com.dio.coinconverter.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.dio.coinconverter.R
import br.com.dio.coinconverter.core.extensions.createDialog
import br.com.dio.coinconverter.core.extensions.createProgressDialog
import br.com.dio.coinconverter.core.extensions.viewBinding
import br.com.dio.coinconverter.databinding.FragmentHistoryBinding
import br.com.dio.coinconverter.databinding.FragmentMainBinding
import br.com.dio.coinconverter.domain.DeleteExchangeUseCase
import br.com.dio.coinconverter.presentation.viewModel.HistoryViewModel
import br.com.dio.coinconverter.presentation.viewModel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel


class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val binding by viewBinding(FragmentHistoryBinding::bind)
    private val viewModel by viewModel<HistoryViewModel>()
    private val adapter by lazy { HistoryListAdapter() }
    private val dialog by lazy { requireContext().createProgressDialog() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvHistory.adapter = adapter
//        binding.rvHistory.addItemDecoration(
//            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
//        )

        bindObserve()

        val swipeGesture = object : SwipeGesture(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val card = adapter.currentList[viewHolder.bindingAdapterPosition]
                viewModel.onTaskSwiped(card)


//                Toasty.success(
//                    requireContext(),
//                    getString(R.string.conversion_excluded),
//                    Toast.LENGTH_SHORT,
//                    true
//                ).show()
            }

        }

        val toucheHelper = ItemTouchHelper(swipeGesture)
        toucheHelper.attachToRecyclerView(binding.rvHistory)


    }

    private fun bindObserve() {
        viewModel.getExchanges()
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                HistoryViewModel.State.Loading -> dialog.show()
                is HistoryViewModel.State.Error -> {
                    dialog.dismiss()
                    requireContext().createDialog {
                        setMessage(it.error.message)
                    }.show()
                }
                is HistoryViewModel.State.Success -> {
                    dialog.dismiss()
                    adapter.submitList(it.list)
                    this.lifecycleScope.launchWhenCreated {
                        viewModel.tasksEvent.collect { event ->
                            when (event) {
                                is HistoryViewModel.TasksEvent.ShowUndoMessange -> {
                                    Snackbar.make(
                                        requireActivity().findViewById(android.R.id.content),
                                        "Cotação Excluída",
                                        Snackbar.LENGTH_LONG
                                    )
                                        .setAction("DESFAZER") {
                                            viewModel.onUndoDeleteClick(event.exchange)
                                        }.show()
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}
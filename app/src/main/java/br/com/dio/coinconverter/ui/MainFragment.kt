package br.com.dio.coinconverter.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import br.com.dio.coinconverter.R
import br.com.dio.coinconverter.core.extensions.*
import br.com.dio.coinconverter.data.model.Coin
import br.com.dio.coinconverter.databinding.FragmentMainBinding
import br.com.dio.coinconverter.presentation.viewModel.MainViewModel
import es.dmoral.toasty.Toasty
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainFragment : Fragment(R.layout.fragment_main) {

    private val binding by viewBinding(FragmentMainBinding::bind)
    private val viewModel by viewModel<MainViewModel>()
    private val dialog by lazy { requireContext().createProgressDialog() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        bindAdapters()
        bindListeners()
        bindObserve()


    }


    private fun Spinner.selected(action: (position: Int) -> Unit) {
        this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                action(position)
            }
        }
    }


    override fun onStart() {
        super.onStart()
        if (!hasInternet()) {
            Toasty.info(
                requireContext(),
                R.string.has_internet,
                Toast.LENGTH_SHORT, true
            ).show()

        }
    }


    private fun bindAdapters() {
        val setAdapter = createSpinnerArrayAdapters()


        with(binding.spinnerFrom) {
            adapter = setAdapter
            setSelection(4)

        }

        with(binding.spinnerTo) {
            adapter = setAdapter
            setSelection(0)

        }
    }


    private fun createSpinnerArrayAdapters(): CoinArrayAdapter {
        val adapter = CoinArrayAdapter(
            requireContext(),
            arrayListOf(
                Currency(R.drawable.br, Coin.BRL.toString()),
                Currency(R.drawable.ar, Coin.ARS.toString()),
                Currency(R.drawable.ca, Coin.CAD.toString()),
                Currency(R.drawable.euro, Coin.EUR.toString()),
                Currency(R.drawable.eua, Coin.USD.toString()),
                Currency(R.drawable.israel, Coin.ILS.toString()),
                Currency(R.drawable.uk, Coin.GBP.toString())
            )
        )

        return (adapter)
    }

    private fun bindListeners() {
        binding.tilValue.editText?.doAfterTextChanged {
            binding.btnConverter.isEnabled = it != null && it.toString().isNotEmpty()
            binding.btnSave.isEnabled = false


        }

        binding.btnConverter.setOnClickListener {
            if (binding.spinnerFrom.selectedItem.equals(binding.spinnerTo.selectedItem)) {
                checkCoins()
            } else {
                it.hideSoftKeyboard()
                val search = "${binding.spinnerFrom.selectedItem}-${binding.spinnerTo.selectedItem}"
                viewModel.getExchangeValue(search)
            }

        }


        binding.btnSave.setOnClickListener {
            val value = viewModel.state.value
            (value as? MainViewModel.State.Success)?.let {
                val exchange =
                    it.exchange.copy(
                        bid = it.exchange.bid * binding.tilValue.text.toDouble(),
                        name = "${binding.tilValue.text} - ${it.exchange.name}"
                    )
                viewModel.saveExchange(exchange)

            }
            binding.btnSave.isEnabled = false
        }


        binding.ibSwap.setOnClickListener {


            val textFrom = binding.spinnerFrom.selectedItemPosition
            val textTo = binding.spinnerTo.selectedItemPosition

            if (textFrom == textTo) {
                checkCoins()
            } else {
                binding.apply {
                    spinnerFrom.animate().apply {
                        duration = 1000
                        rotationXBy(360f)
                    }.start()
                    spinnerTo.animate().apply {
                        duration = 1000
                        rotationXBy(360f)
                    }.start()
                    ibSwap.animate().apply {
                        duration = 1000
                        rotationYBy(360f)
                    }.start()


                    binding.spinnerFrom.setSelection(textTo)
                    binding.spinnerTo.setSelection(textFrom)



                    if (binding.tilValue.text.isNotEmpty()) {
                        val search =
                            "${binding.spinnerFrom.selectedItem}-${binding.spinnerTo.selectedItem}"
                        viewModel.getExchangeValue(search)
                    }
                }


            }

        }
    }

    private fun checkCoins() {
        requireContext().createDialog {
            setMessage("Escolha moedas diferente!")
        }.show()
    }

    private fun bindObserve() {
        viewModel.state.observe(viewLifecycleOwner, { it ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                when (it) {
                    MainViewModel.State.Loading -> dialog.show()
                    MainViewModel.State.Saved -> {
                        dialog.dismiss()
                        Toasty.success(
                            requireContext(),
                            getString(R.string.saved_conversion),
                            Toast.LENGTH_SHORT,
                            true
                        ).show()

                    }
                    is MainViewModel.State.Error -> {
                        dialog.dismiss()
                        if (!hasInternet()) {
                            Toasty.info(
                                requireContext(),
                                R.string.has_internet,
                                Toast.LENGTH_SHORT, true
                            ).show()
                        } else {
                            requireContext().createDialog {
                                setMessage(it.error.message)
                            }.show()
                        }


                    }
                    is MainViewModel.State.Success -> {
                        dialog.dismiss()
                        binding.btnSave.isEnabled = true

                        val selectedCoin = binding.spinnerTo.selectedItem.toString()
                        val coin = Coin.values().find { it.name == selectedCoin } ?: Coin.BRL

                        val result = it.exchange.bid * binding.tilValue.text.toDouble()

                        binding.tvResult.text = result.formatCurrency(coin.locale)

                        Log.e("TAG", "onViewCreated: ${it.exchange}")

                    }
                }
            }

        })
    }

}
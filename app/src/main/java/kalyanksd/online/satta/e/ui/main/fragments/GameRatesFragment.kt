package kalyanksd.online.satta.e.ui.main.fragments

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kalyanksd.online.satta.e.web_service.model.MarketRates
import kalyanksd.online.satta.e.databinding.FragmentGameRatesBinding
import kalyanksd.online.satta.e.ui.main.adapters.GameRatesAdapter
import kalyanksd.online.satta.e.ui.main.viewmodel.MainViewModel
import kalyanksd.online.satta.e.basic_utils.BasicUtils.hideSoftKeyboard
import kalyanksd.online.satta.e.basic_utils.BasicUtils.showErrorSnackBar
import kalyanksd.online.satta.e.basic_utils.Constants
import kalyanksd.online.satta.e.basic_utils.Status
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class GameRatesFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentGameRatesBinding
    private val marketRatesList: ArrayList<MarketRates> = ArrayList()
    private var gameRatesAdapter: GameRatesAdapter? = null
    private lateinit var mView: View
    private lateinit var alertDialog: ProgressDialog
    private lateinit var mContext: Context


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
        viewModel.marketRates()
        alertDialog = ProgressDialog(mContext)
        alertDialog.setMessage(Constants.LOADING_MESSAGE)
        alertDialog.setCanceledOnTouchOutside(false)
        observeGameRates()
    }

    private fun observeGameRates() {
        viewModel.rates.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    alertDialog.dismiss()
                    it.data?.let { list ->
                        if (it.data.isEmpty()) {
                            requireActivity().showErrorSnackBar("Error")
                        } else
                            initRecyclerView(list)
                    }
                }
                Status.LOADING -> {
                    alertDialog.show()
                }
                Status.ERROR -> {
                    alertDialog.dismiss()
                    requireActivity().hideSoftKeyboard(mView)
                    requireActivity().showErrorSnackBar("Error")
                }
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameRatesBinding.inflate(inflater, container, false)
        return binding.root
    }


    private fun initRecyclerView(data: List<MarketRates>) {
        val recyclerView = binding.recyclerView
        marketRatesList.clear()
        marketRatesList.addAll(data)
        gameRatesAdapter?.notifyDataSetChanged()
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        gameRatesAdapter = GameRatesAdapter(marketRatesList)
        recyclerView.adapter = gameRatesAdapter
        recyclerView.setHasFixedSize(false)
    }

}
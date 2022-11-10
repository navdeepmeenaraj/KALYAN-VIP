package kalyanksd.online.satta.e.ui.main.fragments

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import kalyanksd.online.satta.e.R
import kalyanksd.online.satta.e.web_service.PaPaDetails
import kalyanksd.online.satta.e.web_service.model.WithdrawRequest
import kalyanksd.online.satta.e.databinding.FragmentWitHdRawBinding
import kalyanksd.online.satta.e.ui.main.viewmodel.MainViewModel
import kalyanksd.online.satta.e.ui.main.viewmodel.PViewModel
import kalyanksd.online.satta.e.basic_utils.BasicUtils
import kalyanksd.online.satta.e.basic_utils.BasicUtils.containsDigit
import kalyanksd.online.satta.e.basic_utils.BasicUtils.hideKeyboard
import kalyanksd.online.satta.e.basic_utils.BasicUtils.hideSoftKeyboard
import kalyanksd.online.satta.e.basic_utils.BasicUtils.showErrorSnackBar
import kalyanksd.online.satta.e.basic_utils.BasicUtils.showSuccessSnackBar
import kalyanksd.online.satta.e.basic_utils.Status
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class WWFragment : Fragment() {

    private var _binding: FragmentWitHdRawBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by viewModels()
    private val pViewModel: PViewModel by viewModels()

    private lateinit var mView: View
    private lateinit var mContext: Context

    private var minWithdrawal: Int? = null

    private lateinit var progressDialog: ProgressDialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentWitHdRawBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view

        progressDialog = ProgressDialog(mContext)
        progressDialog.setMessage("Sending Withdrawal Request")
        progressDialog.setCanceledOnTouchOutside(false)


        observeUserBankDetails()

        getUserBankDetails()

        getPaymentDetails()?.observe(viewLifecycleOwner, {
            binding.withdrawFundsTitle.text = it.withdrawal_time_title
            minWithdrawal = it.min_withdrawal
        })


        binding.withdrawHistory.setOnClickListener {
            findNavController().navigate(R.id.action_withdrawFragment_to_fragmentWithdrawRequest)
        }
        binding.withdrawFundsButton.setOnClickListener {
            validateParams()
        }

        binding.walletButton.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.walletFragment)
        }

        mainViewModel.withdraw.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.LOADING -> {
                    progressDialog.show()
                }

                Status.SUCCESS -> {
                    binding.withdrawFundsPoints.setText("")
                    binding.withdrawFundsPaymentNumber.setText("")
                    progressDialog.dismiss()
                    hideKeyboard(requireActivity())
                    requireActivity().showSuccessSnackBar("Request Sent Successfully")
                }

                Status.ERROR -> {
                    progressDialog.dismiss()
                    hideKeyboard(requireActivity())
                    requireActivity().showErrorSnackBar("Unable to send request ${it.message}")
                }
            }
        })
    }

    private fun observeUserBankDetails() {
        pViewModel.userBankDetails.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.dismiss()
                    if (it.data?.status == true) {
                        val list: ArrayList<String> = arrayListOf()
                        if (containsDigit(it.data.paytm)) {
                            list.add("Paytm(" + it.data.paytm + ")")
                        }
                        if (containsDigit(it.data.phonepe)) {
                            list.add("PhonePe(" + it.data.phonepe + ")")
                        }
                        if (containsDigit(it.data.gpay)) {
                            list.add("Google Pay(" + it.data.gpay + ")")
                        }

                        binding.paymentAppSpinner.setItems(list)

                    } else {
                        binding.paymentAppSpinner.isEnabled = false
                        binding.paymentAppSpinner.text = "Bank Details not Added"
                    }

                }
                Status.LOADING -> {
                    progressDialog.show()
                }
                Status.ERROR -> {
                    progressDialog.dismiss()
                }
            }

        }
    }

    private fun getUserBankDetails() {
        pViewModel.getUserBankDetails()
    }

    private fun validateParams() {

        if (binding.withdrawFundsPoints.text.toString().isEmpty()) {
            requireActivity().showErrorSnackBar("Enter Points to Withdraw")
            requireActivity().hideSoftKeyboard(mView)
            return
        }


        val points = binding.withdrawFundsPoints.text.toString().toInt()


        if (points < minWithdrawal!!.toInt()) {
            requireActivity().showErrorSnackBar("Minimum Withdrawal Amount $minWithdrawal")
            requireActivity().hideSoftKeyboard(mView)
            return
        }

        if (Prefs.getInt("Balance", 0) < points) {
            requireActivity().showErrorSnackBar("Insufficient Points in your Wallet")
            requireActivity().hideSoftKeyboard(mView)
            return
        }


        if (binding.paymentAppSpinner.selectedIndex == -1) {
            requireActivity().showErrorSnackBar("Select Payment App")
            requireActivity().hideSoftKeyboard(mView)
            return
        }


        val paymentApp = binding.paymentAppSpinner.text.toString()


        mainViewModel.withdrawRequest(
            WithdrawRequest(
                BasicUtils.userId(),
                points.toString(),
                "$paymentApp"
            )
        )


    }

    private fun getPaymentDetails(): LiveData<PaPaDetails>? {
        return mainViewModel.dbPaymentData(mContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.paymentAppSpinner.dismiss()
    }
}
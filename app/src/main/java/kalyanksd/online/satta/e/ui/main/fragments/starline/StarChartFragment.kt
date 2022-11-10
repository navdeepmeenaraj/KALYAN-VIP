package kalyanksd.online.satta.e.ui.main.fragments.starline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kalyanksd.online.satta.e.R
import kalyanksd.online.satta.e.databinding.FragmentStarChartBinding
import kalyanksd.online.satta.e.basic_utils.Constants
import com.pixplicity.easyprefs.library.Prefs


class StarChartFragment : Fragment() {

    private lateinit var binding: FragmentStarChartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStarChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.webviewChart.settings.javaScriptEnabled = true

        binding.formTitle.text = getString(R.string.app_name) + " Starline Chart"
        try {
            binding.webviewChart.loadUrl(Prefs.getString(Constants.STAR_CHART, "null"))
        } catch (e: Exception) {
            throw e
        }
    }
}
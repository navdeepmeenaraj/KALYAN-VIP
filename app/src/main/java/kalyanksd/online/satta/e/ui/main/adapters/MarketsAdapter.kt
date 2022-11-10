package kalyanksd.online.satta.e.ui.main.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.CountDownTimer
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kalyanksd.online.satta.e.R
import kalyanksd.online.satta.e.web_service.model.MarketData
import kalyanksd.online.satta.e.basic_utils.BasicUtils.checkIfUserIsVerified
import kalyanksd.online.satta.e.basic_utils.BasicUtils.lastDigit
import kalyanksd.online.satta.e.basic_utils.BasicUtils.sumOfDigits
import kalyanksd.online.satta.e.basic_utils.BasicUtils.toDate
import kalyanksd.online.satta.e.basic_utils.Constants.PREFS_CHART_ID
import kalyanksd.online.satta.e.basic_utils.Constants.PREFS_CHART_TITLE
import kalyanksd.online.satta.e.basic_utils.Constants.PREFS_MARKET_ID
import kalyanksd.online.satta.e.basic_utils.Constants.PREFS_MARKET_NAME
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.pixplicity.easyprefs.library.Prefs
import kalyanksd.online.satta.e.basic_utils.BasicUtils
import kotlinx.android.synthetic.main.item_market_list_2.view.*
import java.text.SimpleDateFormat
import java.util.*

class MarketsAdapter(private var markets: List<MarketData>) :
    RecyclerView.Adapter<MarketsAdapter.MarketViewHolder>() {
    var position: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MarketViewHolder {

        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_market_list_2, viewGroup, false)

        if (markets[position].market_status != 0) {
            view.countdown_time.visibility = View.VISIBLE
            val formatter = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.ENGLISH);
            formatter.isLenient = false;

            val endTime = "${BasicUtils.getCurrentDate2()}, ${markets[position].market_close_time}"

            val endDate: Date = formatter.parse(endTime);
            val milliseconds = endDate.getTime();

            val startTime = System.currentTimeMillis();

            milliseconds - startTime
            object : CountDownTimer(milliseconds, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val startTime2 = startTime - 1
                    val serverUptimeSeconds: Long = (millisUntilFinished - startTime2) / 1000

                    if (millisUntilFinished < startTime) {
                        view.countdown_time.visibility = View.GONE
                        view.market_status.text = "Closed"
                        view.market_status.setTextColor(Color.parseColor("#ff4757"))
                        return
                    }

                    val hoursLeft = String.format("%d", serverUptimeSeconds % 86400 / 3600)

                    val minutesLeft = String.format("%d", serverUptimeSeconds % 86400 % 3600 / 60)

                    val secondsLeft = String.format("%d", serverUptimeSeconds % 86400 % 3600 % 60)
                    view.countdown_time.text = "Time left $hoursLeft : $minutesLeft : $secondsLeft"

                }

                override fun onFinish() {
                    view.countdown_time.visibility = View.GONE
                }
            }.start()

        }

        position++
        return MarketViewHolder(view)
    }


    override fun getItemCount() = markets.size

    override fun onBindViewHolder(holder: MarketViewHolder, position: Int) {
        holder.bind(markets[position])
    }

    class MarketViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val marketName = view.market_name
        private val closeResult = view.close_result
        private val openResult = view.open_result
        private val openPanel = view.open_panel
        private val closePanel = view.close_panel
        private val marketOpenTime = view.market_open_time
        private val marketCloseTime = view.market_close_time
        private val playButton = view.play_button
        private val marketChart = view.chart
        private val marketStatus = view.market_status

        @SuppressLint("SetTextI18n")
        fun bind(markets: MarketData) {
            marketName.text = markets.market_name
            openPanel.text = markets.open_pana
            closePanel.text = markets.close_pana

            if (!checkIfUserIsVerified()) {
                playButton.visibility = View.GONE
                marketStatus.visibility = View.GONE
            }

            if (markets.market_status == 0) {
                playButton.setBackgroundResource(R.drawable.kvip_ic_baseline_play_red_24)
                marketStatus.setTextColor(Color.parseColor("#ff3838"))
                marketStatus.text = "Closed"
                playButton.setOnClickListener {
                    YoYo.with(Techniques.Shake)
                        .duration(500)
                        .playOn(marketStatus)
                    val vibe =
                        it.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibe.vibrate(100)
                }
            } else {
                playButton.setBackgroundResource(R.drawable.kvip_ic_baseline_play_circle_outline_24)
                marketStatus.setTextColor(Color.parseColor("#2ed573"))
                marketStatus.text = "Running"
                playButton.setOnClickListener { view ->
                    handleButtonOnClick(markets.market_id, markets.market_name, view)
                }

            }

            try {
                openResult.text = if (markets.open_pana != "***") {
                    lastDigit(sumOfDigits(markets.open_pana)).toString()
                } else {
                    "*"
                }

                closeResult.text = if (markets.close_pana != "***") {
                    lastDigit(sumOfDigits(markets.close_pana)).toString()
                } else {
                    "*"
                }

            } catch (e: NumberFormatException) {
                throw e
            }


            marketChart.setOnClickListener {
                showMarketChart(markets.market_id, markets.market_name, it)
            }

            marketOpenTime.text = "Open: " + toDate(markets.market_open_time)
            marketCloseTime.text = "Close: " + toDate(markets.market_close_time)
        }

        private fun showMarketChart(marketId: Int, marketName: String, view: View) {
            Prefs.putInt(PREFS_CHART_ID, marketId)
            Prefs.putString(PREFS_CHART_TITLE, marketName)
            Navigation.findNavController(view)
                .navigate(R.id.chartFragment)
        }

        private fun handleButtonOnClick(marketId: Int, marketName: String, view: View) {
            Prefs.putInt(PREFS_MARKET_ID, marketId)
            Prefs.putString(PREFS_MARKET_NAME, marketName)
            Navigation.findNavController(view)
                .navigate(R.id.betTypeFragment)
        }
    }
}
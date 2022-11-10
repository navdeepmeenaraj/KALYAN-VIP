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
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kalyanksd.online.satta.e.R
import kalyanksd.online.satta.e.web_service.model.starline.StarlineMarkets
import kalyanksd.online.satta.e.basic_utils.BasicUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.pixplicity.easyprefs.library.Prefs
import java.text.SimpleDateFormat
import java.util.*

class StarlineMarketAdapter(private var markets: List<StarlineMarkets>) :
    RecyclerView.Adapter<StarlineMarketAdapter.StarlineMarketViewHolder>() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): StarlineMarketViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.starmarket_items, parent, false)
        return StarlineMarketViewHolder(view)
    }

    override fun getItemCount() = markets.size

    override fun onBindViewHolder(holder: StarlineMarketViewHolder, position: Int) {
        holder.bind(markets[position])
    }

    class StarlineMarketViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val marketStarline = view.findViewById<TextView>(R.id.starline_time)
        private val panelStarline = view.findViewById<TextView>(R.id.starline_open_panel)
        private val resultStarline = view.findViewById<TextView>(R.id.starline_open_result)
        private val buttonPlayStarLine = view.findViewById<ImageButton>(R.id.starline_play_button)
        private val statusStarLine = view.findViewById<TextView>(R.id.starline_market_status)
        val starCountDown = view.findViewById<TextView>(R.id.star_count_down)
        var countDownTimer: CountDownTimer? = null

        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("SetTextI18n")
        fun bind(markets: StarlineMarkets) {
            markets.apply {
                panelStarline.text = open_panel
                marketStarline.text = BasicUtils.toDate(markets.close_time)
                statusStarLine.text = BasicUtils.convertToStatus(markets.status)
                resultStarline.text = markets.open_panel
            }

            YoYo.with(Techniques.SlideInLeft)
                .duration(300)
                .playOn(resultStarline)
            YoYo.with(Techniques.SlideInLeft)
                .duration(300)
                .playOn(panelStarline)
            resultStarline.text = if (markets.open_panel != "***") {
                BasicUtils.lastDigit(BasicUtils.sumOfDigits(markets.open_panel)).toString()
            } else {
                "*"
            }

            if (markets.status == 0) {
                buttonPlayStarLine.setBackgroundResource(R.drawable.kvip_play_icon_red)
                statusStarLine.setTextColor(Color.parseColor("#ff4757"))
                buttonPlayStarLine.setOnClickListener {
                    YoYo.with(Techniques.Shake)
                        .duration(100)
                        .playOn(buttonPlayStarLine)
                    val vibe =
                        itemView.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibe.vibrate(100)
                }
            } else {
                buttonPlayStarLine.setBackgroundResource(R.drawable.kvip_play_icon_green)
                starCountDown.visibility = View.VISIBLE
                val formatter = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.ENGLISH);
                formatter.isLenient = false
                val endTime = "${BasicUtils.getCurrentDate2()}, ${markets.close_time}"
                val endDate: Date = formatter.parse(endTime)
                val milliseconds = endDate.time

                val startTime = System.currentTimeMillis()
                milliseconds - startTime

                countDownTimer = object : CountDownTimer(milliseconds, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val startTime2 = startTime - 1
                        val serverUptimeSeconds: Long = (millisUntilFinished - startTime2) / 1000

                        if (millisUntilFinished < startTime) {
                            starCountDown.visibility = View.GONE
                            statusStarLine.text = "Closed"
                            statusStarLine.setTextColor(Color.parseColor("#ff4757"))
                            return
                        }
                        val hoursLeft = String.format("%d", serverUptimeSeconds % 86400 / 3600)
                        val minutesLeft =
                            String.format("%d", serverUptimeSeconds % 86400 % 3600 / 60)
                        val secondsLeft =
                            String.format("%d", serverUptimeSeconds % 86400 % 3600 % 60)
                        starCountDown.text =
                            "Time left $hoursLeft : $minutesLeft : $secondsLeft"
                    }

                    override fun onFinish() {
                        starCountDown.visibility = View.GONE
                    }
                }.start()
                statusStarLine.setTextColor(Color.parseColor("#2ecc71"))
                buttonPlayStarLine.setOnClickListener { view ->
                    handleButtonOnClick(markets.id, markets.close_time, view)
                }

            }
        }

        private fun handleButtonOnClick(starMarketId: Int, starCloseTime: String, view: View) {
            Prefs.putInt("star_market_id", starMarketId)
            Prefs.putString("star_market_time", starCloseTime)
            Navigation.findNavController(view)
                .navigate(R.id.action_starLineDashboard_to_starLineMarkets)
        }
    }

}
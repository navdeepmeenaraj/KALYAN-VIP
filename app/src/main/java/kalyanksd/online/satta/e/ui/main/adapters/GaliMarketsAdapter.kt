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
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kalyanksd.online.satta.e.R
import kalyanksd.online.satta.e.web_service.model.gali.GaliMarkets
import kalyanksd.online.satta.e.basic_utils.BasicUtils
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.gali_market_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class GaliMarketsAdapter(private var gali: List<GaliMarkets>) :
    RecyclerView.Adapter<GaliMarketsAdapter.GaliMarketsViewHolder>() {
    var position: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): GaliMarketsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.gali_market_item, parent, false)


        if (gali[position].is_closed != 0) {
            view.gali_count_down.visibility = View.VISIBLE
            val formatter = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.ENGLISH);
            formatter.isLenient = false

            val endTime = "${BasicUtils.getCurrentDate2()}, ${gali[position].open_time}"

            val endDate: Date = formatter.parse(endTime)
            val milliseconds = endDate.time;

            val startTime = System.currentTimeMillis();

            milliseconds - startTime
            object : CountDownTimer(milliseconds, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val startTime2 = startTime - 1
                    val serverUptimeSeconds: Long = (millisUntilFinished - startTime2) / 1000

                    if (millisUntilFinished < startTime) {
                        view.gali_count_down.visibility = View.GONE
                        view.textview_status_subtext.text = "Closed"
                        view.textview_status_subtext.setTextColor(Color.parseColor("#ff4757"))
                        return
                    }
                    val hoursLeft = String.format("%d", serverUptimeSeconds % 86400 / 3600)
                    val minutesLeft = String.format("%d", serverUptimeSeconds % 86400 % 3600 / 60)
                    val secondsLeft = String.format("%d", serverUptimeSeconds % 86400 % 3600 % 60)
                    view.gali_count_down.text = "Time left $hoursLeft : $minutesLeft : $secondsLeft"

                }

                override fun onFinish() {
                    view.gali_count_down.visibility = View.GONE
                }
            }.start()

        }


        position++
        return GaliMarketsViewHolder(view)
    }

    override fun getItemCount() = gali.size

    override fun onBindViewHolder(holder: GaliMarketsViewHolder, position: Int) {
        holder.bind(gali[position])
    }

    class GaliMarketsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val galiName = view.findViewById<TextView>(R.id.textview_gali_name)
        private val galiTime = view.findViewById<TextView>(R.id.textview_gali_time)
        private val galiResult = view.findViewById<TextView>(R.id.textview_gali_result)
        private val galiStatus = view.findViewById<TextView>(R.id.textview_status_subtext)
        private val galiPlayButton = view.findViewById<ImageView>(R.id.buttonGaliPlay)

        @SuppressLint("SetTextI18n")
        fun bind(gali: GaliMarkets) {
            gali.apply {
                galiName.text = gali_name
                galiTime.text = "Result Time- " + BasicUtils.toDate(gali.open_time)
                galiStatus.text = BasicUtils.convertToStatus(gali.is_closed)
                galiResult.text = gali.result
            }
            if (gali.is_closed == 0) {
                galiStatus.setTextColor(Color.parseColor("#ff4757"))
                galiPlayButton.setOnClickListener {
                    val vibe =
                        itemView.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibe.vibrate(100)
                }
            } else {
                galiStatus.setTextColor(Color.parseColor("#2ecc71"))
                galiPlayButton.setOnClickListener { view ->
                    handleButtonOnClick(gali.id, gali.open_time, view)
                }
            }
        }


        private fun getMarketDat() {


        }


        private fun handleButtonOnClick(galiId: Int, galiTime: String, view: View) {
            Prefs.putInt("gali_id", galiId)
            Prefs.putString("gali_time", galiTime)
            Navigation.findNavController(view)
                .navigate(R.id.action_galiMarketsFragment_to_galiPlaceForm)
        }
    }

}
package com.rhinepereira.saints.data.nameday

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.rhinepereira.saints.MainActivity
import com.rhinepereira.saints.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.MonthDay
import java.time.ZoneId

/** A person and the patron saint whose feast is their name-day. */
data class NameDay(val person: String, val saint: String, val month: Int, val day: Int) {
    val monthDay: MonthDay get() = MonthDay.of(month, day)

    fun nextOn(from: LocalDate): LocalDate {
        // Feb 29 patrons fall back to Feb 28 in common years.
        fun inYear(y: Int) = if (monthDay.isValidYear(y)) monthDay.atYear(y) else LocalDate.of(y, 2, 28)
        val thisYear = inYear(from.year)
        return if (thisYear.isBefore(from)) inYear(from.year + 1) else thisYear
    }
}

class NameDayStore(private val context: Context) {

    private val prefs = context.getSharedPreferences("namedays", Context.MODE_PRIVATE)
    private val _all = MutableStateFlow(load())
    val all: StateFlow<List<NameDay>> = _all.asStateFlow()

    fun add(n: NameDay) = save(_all.value + n)
    fun remove(n: NameDay) = save(_all.value - n)

    fun on(date: LocalDate): List<NameDay> =
        _all.value.filter { it.nextOn(date) == date }

    private fun save(list: List<NameDay>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("p", it.person).put("s", it.saint).put("m", it.month).put("d", it.day))
        }
        prefs.edit().putString("list", arr.toString()).apply()
        _all.value = list
        NameDayAlarm.schedule(context, list)
    }

    private fun load(): List<NameDay> {
        val arr = JSONArray(prefs.getString("list", "[]"))
        return List(arr.length()) { i ->
            arr.getJSONObject(i).run { NameDay(getString("p"), getString("s"), getInt("m"), getInt("d")) }
        }
    }
}

/** One inexact alarm at 07:30 on the nearest upcoming name-day; it re-arms itself after firing. */
object NameDayAlarm {
    private const val CHANNEL = "namedays_channel"

    fun schedule(context: Context, list: List<NameDay>) {
        val am = context.getSystemService(AlarmManager::class.java)
        val pi = PendingIntent.getBroadcast(
            context, 7, Intent(context, NameDayReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
        val now = java.time.LocalDateTime.now()
        val today = now.toLocalDate()
        val start = if (now.toLocalTime().isBefore(LocalTime.of(7, 30))) today else today.plusDays(1)
        val next = list.minOfOrNull { it.nextOn(start) } ?: return
        val at = next.atTime(7, 30).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        am.set(AlarmManager.RTC_WAKEUP, at, pi)
    }

    fun notifyToday(context: Context, people: List<NameDay>) {
        if (people.isEmpty()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return
        val nm = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, context.getString(R.string.nameday_channel), NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val open = PendingIntent.getActivity(
            context, 8,
            Intent(Intent.ACTION_VIEW, Uri.parse("saints://today"), context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        people.forEachIndexed { i, n ->
            nm.notify(
                1000 + i,
                NotificationCompat.Builder(context, CHANNEL)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(ContextCompat.getColor(context, R.color.notification_color))
                    .setContentTitle(context.getString(R.string.nameday_greeting, n.person))
                    .setContentText(context.getString(R.string.nameday_patron, n.saint))
                    .setContentIntent(open)
                    .setAutoCancel(true)
                    .build()
            )
        }
    }
}

class NameDayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val store = NameDayStore(context)
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            NameDayAlarm.notifyToday(context, store.on(LocalDate.now()))
        }
        NameDayAlarm.schedule(context, store.all.value)
    }
}

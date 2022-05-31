package com.example.navigationdrawer

import java.util.concurrent.TimeUnit

private const val SECOND_MILLIS = 1
private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
private const val DAY_MILLIS = 24 * HOUR_MILLIS
private const val MONTH_MILLIS = 30 * DAY_MILLIS

object TimeUtils{
    fun getTimeAgo(time:Int):String{
        val now = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        if (time > now || time<=0){
            return "in the future"
        }

        val diff = now - time
        return when{
            diff < MINUTE_MILLIS -> "şimdi"
            diff < 2 * MINUTE_MILLIS -> "1 dk"
            diff < 60 * MINUTE_MILLIS -> "${diff/ MINUTE_MILLIS} dk"
            diff < 2 * HOUR_MILLIS -> "1 s"
            diff < 24 * HOUR_MILLIS -> "${diff/ HOUR_MILLIS} s"
            diff < 48 * HOUR_MILLIS -> "dün"
            else -> "${diff/ DAY_MILLIS} gün"
        }
    }

    fun checkTimeAgo(time: Int):Int{
        val now = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())

        val diff = now - time
        return (diff/ HOUR_MILLIS).toInt()
    }

}
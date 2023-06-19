package com.example.storyapp.helper

import java.text.SimpleDateFormat
import java.util.*

object Helper {

    fun String.withDateFormat(userLocale: Locale): String {
        val sourcePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        val destPattern = "dd MMM yyyy, HH:mm"
        val sourceFormatter = SimpleDateFormat(sourcePattern, Locale.getDefault())
        sourceFormatter.timeZone = TimeZone.getTimeZone("GMT")
        val destFormatter = SimpleDateFormat(destPattern, userLocale)
        val timeZone = TimeZone.getDefault()
        destFormatter.timeZone = timeZone
        val date = sourceFormatter.parse(this) as Date
        return destFormatter.format(date)
    }

}
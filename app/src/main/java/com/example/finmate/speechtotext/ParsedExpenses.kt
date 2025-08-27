package com.example.finmate.speechtotext

import com.example.finmate.model.Expenses
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt



fun parseSpeech(text: String): Expenses {
    val words = text.lowercase().split(" ")
    val fullText = text.lowercase()

    // 1. Extract Amount
    val amountRegex = Regex("""\d+(\.\d{1,2})?""")
    val amount = amountRegex.findAll(text)
        .mapNotNull { it.value.toDoubleOrNull() }
        .maxOrNull()
        ?.roundToInt()
        ?.toString() ?: "N/A"

    // 2. Extract Category
    var category = "Others"
    for (word in words) {
        keywordCategoryMap.forEach { (keyword, cat) ->
            if (word.contains(keyword, true)) {
                category = cat
                return@forEach
            }
        }
    }

    // 3. Title (first matched keyword)
    val title = words.find { word ->
        keywordCategoryMap.keys.any { keyword -> word.contains(keyword) }
    }?.replaceFirstChar { it.uppercaseChar() } ?: "Unknown"

    // 4. Extract Date
    val sdfDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    var calendar = Calendar.getInstance()
    var dateStr = sdfDate.format(calendar.time)

    when {
        fullText.contains("yesterday") -> {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            dateStr = sdfDate.format(calendar.time)
        }
        fullText.contains("today") -> {
            dateStr = sdfDate.format(calendar.time)
        }
        else -> {
            // Improved Regex - Handles formats like "12th July", "July 12", etc.
            val dateRegex = Regex("""(?:(\d{1,2})(?:st|nd|rd|th)?[\s\-]*(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|january|february|march|april|june|july|august|september|october|november|december))|(?:(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|january|february|march|april|june|july|august|september|october|november|december)[\s\-]*(\d{1,2}))""")

            val match = dateRegex.find(fullText)
            if (match != null) {
                val day = match.groups[1]?.value ?: match.groups[4]?.value
                val monthRaw = match.groups[2]?.value ?: match.groups[3]?.value

                val month = when (monthRaw?.take(3)?.lowercase()) {
                    "jan" -> "01"
                    "feb" -> "02"
                    "mar" -> "03"
                    "apr" -> "04"
                    "may" -> "05"
                    "jun" -> "06"
                    "jul" -> "07"
                    "aug" -> "08"
                    "sep" -> "09"
                    "oct" -> "10"
                    "nov" -> "11"
                    "dec" -> "12"
                    else -> null
                }

                val year = calendar.get(Calendar.YEAR).toString()
                if (day != null && month != null) {
                    val formattedDay = day.padStart(2, '0')
                    dateStr = "$formattedDay-$month-$year"
                }
            }
        }
    }

    // 5. Extract Time
    val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
    calendar = Calendar.getInstance()
    var timeStr = sdfTime.format(calendar.time)

    val timeRegex = Regex("""(?:at\s*)?(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""", RegexOption.IGNORE_CASE)
    val timeMatch = timeRegex.find(fullText)

    if (timeMatch != null) {
        val hour = timeMatch.groups[1]?.value?.toIntOrNull() ?: 0
        val minute = timeMatch.groups[2]?.value?.toIntOrNull() ?: 0
        val ampm = timeMatch.groups[3]?.value?.lowercase()

        if (ampm == "pm" && hour < 12) {
            calendar.set(Calendar.HOUR, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.AM_PM, Calendar.PM)
        } else if (ampm == "am" || hour == 12) {
            calendar.set(Calendar.HOUR, hour % 12)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.AM_PM, Calendar.AM)
        } else {
            // No AM/PM - assume based on context or default
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
        }
        timeStr = sdfTime.format(calendar.time)
    } else {
        // Natural language fallback
        when {
            fullText.contains("morning") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
            }
            fullText.contains("afternoon") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 14)
                calendar.set(Calendar.MINUTE, 0)
            }
            fullText.contains("evening") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 19)
                calendar.set(Calendar.MINUTE, 0)
            }
            fullText.contains("night") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 21)
                calendar.set(Calendar.MINUTE, 0)
            }
            fullText.contains("noon") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 12)
                calendar.set(Calendar.MINUTE, 0)
            }
            fullText.contains("midnight") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
            }
        }
        timeStr = sdfTime.format(calendar.time)
    }


    return Expenses(
        title = title,
        amount = amount,
        category = category,
        date = dateStr,
        time = timeStr
    )
}
package com.example.finmate.speechtotext

import com.example.finmate.model.Expenses
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

fun parseSpeech(text: String): Expenses {
    val lower = text.lowercase()
    val words = lower.split(" ")

    // 1) Amount (largest number in sentence)
    val amountRegex = Regex("""\d+(\.\d{1,2})?""")
    val amount = amountRegex.findAll(lower)
        .mapNotNull { it.value.toDoubleOrNull() }
        .maxOrNull()?.roundToInt()?.toString() ?: "0"

    // 2) Category via existing keyword map (keeps your map unchanged)
    var category = "Others"
    for (word in words) {
        keywordCategoryMap.forEach { (keyword, cat) ->
            if (word.contains(keyword, true)) {
                category = cat
                return@forEach
            }
        }
    }

    // 3) Title (first meaningful word)
    val title = words.firstOrNull()?.replaceFirstChar { it.uppercaseChar() } ?: "Expense"

    // 4) Date — produce dd-MM-yyyy (voice parser uses this)
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()
    var dateStr = dateFormat.format(calendar.time)
    if (lower.contains("yesterday")) {
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        dateStr = dateFormat.format(calendar.time)
    } else if (lower.contains("today")) {
        dateStr = dateFormat.format(calendar.time)
    } else {
        // advanced date parsing could be added here (optional)
    }

    // 5) Time
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    var timeStr = timeFormat.format(Date())
    val timeRegex = Regex("""(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""")
    val match = timeRegex.find(lower)
    if (match != null) {
        val hour = match.groups[1]?.value?.toIntOrNull() ?: 0
        val minute = match.groups[2]?.value?.toIntOrNull() ?: 0
        val ampm = match.groups[3]?.value?.lowercase()
        val tcal = Calendar.getInstance()
        if (ampm == "pm" && hour < 12) tcal.set(Calendar.AM_PM, Calendar.PM)
        else tcal.set(Calendar.AM_PM, Calendar.AM)
        tcal.set(Calendar.HOUR, hour)
        tcal.set(Calendar.MINUTE, minute)
        timeStr = timeFormat.format(tcal.time)
    } else {
        // natural language fallback
        when {
            lower.contains("noon") -> { calendar.set(Calendar.HOUR_OF_DAY, 12); calendar.set(Calendar.MINUTE, 0) }
            lower.contains("midnight") -> { calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0) }
            lower.contains("morning") -> { calendar.set(Calendar.HOUR_OF_DAY, 9); calendar.set(Calendar.MINUTE, 0) }
            lower.contains("afternoon") -> { calendar.set(Calendar.HOUR_OF_DAY, 15); calendar.set(Calendar.MINUTE, 0) }
            lower.contains("evening") -> { calendar.set(Calendar.HOUR_OF_DAY, 19); calendar.set(Calendar.MINUTE, 0) }
            lower.contains("night") -> { calendar.set(Calendar.HOUR_OF_DAY, 21); calendar.set(Calendar.MINUTE, 0) }
        }
        timeStr = timeFormat.format(calendar.time)
    }

    return Expenses(
        id = "",                // empty — saver will set id
        title = title,
        amount = amount,
        category = category,
        date = dateStr,         // dd-MM-yyyy (important)
        time = timeStr
    )
}

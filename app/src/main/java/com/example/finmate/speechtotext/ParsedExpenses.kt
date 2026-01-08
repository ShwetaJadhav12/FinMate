package com.example.finmate.speechtotext

import com.example.finmate.model.Expenses
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// --- DATE: dd/MM/yyyy with natural phrases ---
fun parseNaturalDate(text: String): String {
    val lower = text.lowercase()
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    when {
        lower.contains("day before yesterday") -> {
            calendar.add(Calendar.DAY_OF_YEAR, -2)
            return dateFormat.format(calendar.time)
        }
        lower.contains("yesterday") -> {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            return dateFormat.format(calendar.time)
        }
        lower.contains("tomorrow") -> {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            return dateFormat.format(calendar.time)
        }
        lower.contains("today") -> return dateFormat.format(calendar.time)
    }

    val days = mapOf(
        "monday" to Calendar.MONDAY,
        "tuesday" to Calendar.TUESDAY,
        "wednesday" to Calendar.WEDNESDAY,
        "thursday" to Calendar.THURSDAY,
        "friday" to Calendar.FRIDAY,
        "saturday" to Calendar.SATURDAY,
        "sunday" to Calendar.SUNDAY
    )

    days.forEach { (name, dConst) ->
        if (lower.contains("last $name")) {
            calendar.set(Calendar.DAY_OF_WEEK, dConst)
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
            return dateFormat.format(calendar.time)
        }
        if (lower.contains(name)) {
            calendar.set(Calendar.DAY_OF_WEEK, dConst)
            return dateFormat.format(calendar.time)
        }
    }

    return dateFormat.format(calendar.time)
}

// --- MAIN SPEECH PARSER ---
fun parseSpeech(text: String): Expenses {
    val lower = text.lowercase()
    val words = lower.split(" ")

    // 1) Amount (largest number)
    val amountRegex = Regex("""\d+(\.\d{1,2})?""")
    val amount = amountRegex.findAll(lower)
        .mapNotNull { it.value.toDoubleOrNull() }
        .maxOrNull()
        ?.roundToInt()
        ?.toString() ?: "0"

    // 2) CATEGORY (FIX: explicit category has priority)
    var category = extractExplicitCategory(lower) ?: "Others"

    if (category == "Others") {
        for (word in words) {
            keywordCategoryMap.forEach { (keyword, cat) ->
                if (word.contains(keyword, true)) {
                    category = cat
                    return@forEach
                }
            }
        }
    }

    // 3) Title
    val title = words.firstOrNull()
        ?.replaceFirstChar { it.uppercaseChar() }
        ?: "Expense"

    // 4) Date
    val dateStr = parseNaturalDate(lower)

    // 5) Time -> 24-hour HH:mm
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    var timeStr = timeFormat.format(Date())

    val timeRegex = Regex("""(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""")
    val match = timeRegex.find(lower)

    if (match != null) {
        val hour = match.groups[1]?.value?.toIntOrNull() ?: 0
        val minute = match.groups[2]?.value?.toIntOrNull() ?: 0
        val ampm = match.groups[3]?.value?.lowercase()

        val tcal = Calendar.getInstance()
        var h = hour

        if (ampm == "pm" && h < 12) h += 12
        if (ampm == "am" && h == 12) h = 0

        tcal.set(Calendar.HOUR_OF_DAY, h)
        tcal.set(Calendar.MINUTE, minute)
        timeStr = timeFormat.format(tcal.time)
    }

    return Expenses(
        id = "",
        title = title,
        amount = amount,
        category = category,
        date = dateStr,   // dd/MM/yyyy
        time = timeStr    // HH:mm
    )
}

// --- EXPLICIT CATEGORY DETECTOR (ADDED) ---
fun extractExplicitCategory(text: String): String? {
    val categories = listOf(
        "food", "groceries", "transport", "shopping", "entertainment",
        "bills", "health", "utensils", "education", "finance", "others"
    )

    for (cat in categories) {
        if (
            text.contains("category $cat") ||
            text.contains("$cat category") ||
            text.contains("$cat expense") ||
            text.contains("for $cat")
        ) {
            return cat.replaceFirstChar { it.uppercaseChar() }
        }
    }
    return null
}

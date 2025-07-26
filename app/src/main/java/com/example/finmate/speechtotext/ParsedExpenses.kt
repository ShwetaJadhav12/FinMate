package com.example.finmate.speechtotext

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class ExtractedInfo(
    val title: String,
    val amount: String,
    val category: String,
    val date: String,
    val time: String
)

fun parseSpeech(text: String): ExtractedInfo {
    val words = text.lowercase().split(" ")

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

    // 3. Extract Title (first matched word from known categories)
    val title = words.find { word ->
        keywordCategoryMap.keys.any { keyword -> word.contains(keyword) }
    }?.replaceFirstChar { it.uppercaseChar() } ?: "Unknown"

    // 4. Date & Time (current time)
    val sdfDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val now = Date()

    return ExtractedInfo(
        title = title,
        amount = amount,
        category = category,
        date = sdfDate.format(now),
        time = sdfTime.format(now)
    )
}

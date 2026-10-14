package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun scheduledRestBreakBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.79
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a scheduled rest break budget score falls in. */
fun classifyScheduledRestBreakBudget(score: Double): String {
    if (score < 26.0) {
        return "high"
    }
    if (score < 62.4) {
        return "watch"
    }
    return "ok"
}

/** Puts a scheduled rest break budget code in its canonical SRBB- form. */
fun normalizeScheduledRestBreakBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRBB-")) {
        return clean
    }
    return "SRBB-${clean.replace(" ", "-")}"
}

fun mergeScheduledRestBreakBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

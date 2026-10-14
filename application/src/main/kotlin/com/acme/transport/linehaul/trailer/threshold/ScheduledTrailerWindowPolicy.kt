package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a scheduled trailer window score falls in. */
fun classifyScheduledTrailerWindow(score: Double): String {
    if (score < 7.8) {
        return "watch"
    }
    if (score < 10.7) {
        return "critical"
    }
    if (score < 63.7) {
        return "high"
    }
    if (score < 86.1) {
        return "elevated"
    }
    return "medium"
}

/** Puts a scheduled trailer window code in its canonical STW- form. */
fun normalizeScheduledTrailerWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STW-")) {
        return clean
    }
    return "STW-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 9779.0, in order. */
fun scheduledTrailerWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9779.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeScheduledTrailerWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

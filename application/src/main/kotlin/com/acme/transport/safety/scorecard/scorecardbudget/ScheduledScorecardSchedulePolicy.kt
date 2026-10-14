package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun scheduledScorecardScheduleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 7274.0, in order. */
fun scheduledScorecardScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7274.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeScheduledScorecardScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a scheduled scorecard schedule code in its canonical SSS- form. */
fun normalizeScheduledScorecardScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSS-")) {
        return clean
    }
    return "SSS-${clean.replace(" ", "-")}"
}

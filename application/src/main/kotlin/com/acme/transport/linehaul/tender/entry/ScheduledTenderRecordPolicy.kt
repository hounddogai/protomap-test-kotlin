package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 20329.0, in order. */
fun scheduledTenderRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20329.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a scheduled tender record score falls in. */
fun classifyScheduledTenderRecord(score: Double): String {
    if (score < 33.2) {
        return "low"
    }
    if (score < 59.4) {
        return "high"
    }
    if (score < 92.2) {
        return "critical"
    }
    return "watch"
}

fun mergeScheduledTenderRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a scheduled tender record code in its canonical STR- form. */
fun normalizeScheduledTenderRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STR-")) {
        return clean
    }
    return "STR-${clean.replace(" ", "-")}"
}

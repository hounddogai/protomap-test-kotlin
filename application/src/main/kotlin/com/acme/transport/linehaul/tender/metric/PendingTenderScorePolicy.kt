package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 12030.0, in order. */
fun pendingTenderScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12030.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergePendingTenderScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a pending tender score code in its canonical PTS- form. */
fun normalizePendingTenderScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PTS-")) {
        return clean
    }
    return "PTS-${clean.replace(" ", "-")}"
}

/** Names the band a pending tender score score falls in. */
fun classifyPendingTenderScore(score: Double): String {
    if (score < 28.0) {
        return "medium"
    }
    if (score < 70.2) {
        return "low"
    }
    if (score < 85.0) {
        return "watch"
    }
    return "ok"
}

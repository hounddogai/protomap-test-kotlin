package com.acme.transport.crewing.roster.summary

import com.acme.transport.kit.numbers.roundTo

/** Puts a roster bundle code in its canonical RB- form. */
fun normalizeRosterBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RB-")) {
        return clean
    }
    return "RB-${clean.replace(" ", "-")}"
}

fun rosterBundleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a roster bundle score falls in. */
fun classifyRosterBundle(score: Double): String {
    if (score < 8.6) {
        return "ok"
    }
    if (score < 12.0) {
        return "critical"
    }
    if (score < 21.2) {
        return "minimal"
    }
    if (score < 41.4) {
        return "watch"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 15023.0, in order. */
fun rosterBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 15023.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

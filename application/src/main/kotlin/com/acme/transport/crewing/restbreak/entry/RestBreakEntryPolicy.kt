package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.kit.numbers.roundTo

/** Names the band a rest break entry score falls in. */
fun classifyRestBreakEntry(score: Double): String {
    if (score < 26.1) {
        return "ok"
    }
    if (score < 34.6) {
        return "low"
    }
    if (score < 44.2) {
        return "watch"
    }
    if (score < 99.1) {
        return "critical"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 71118.0, in order. */
fun restBreakEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71118.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun restBreakEntryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a rest break entry code in its canonical RBE- form. */
fun normalizeRestBreakEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBE-")) {
        return clean
    }
    return "RBE-${clean.replace(" ", "-")}"
}

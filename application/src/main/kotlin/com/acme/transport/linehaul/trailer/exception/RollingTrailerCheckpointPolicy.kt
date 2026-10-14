package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.numbers.roundTo

fun rollingTrailerCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 7516.0, in order. */
fun rollingTrailerCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7516.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a rolling trailer checkpoint code in its canonical RTC- form. */
fun normalizeRollingTrailerCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTC-")) {
        return clean
    }
    return "RTC-${clean.replace(" ", "-")}"
}

/** Names the band a rolling trailer checkpoint score falls in. */
fun classifyRollingTrailerCheckpoint(score: Double): String {
    if (score < 4.7) {
        return "low"
    }
    if (score < 45.8) {
        return "high"
    }
    if (score < 69.0) {
        return "minimal"
    }
    if (score < 73.9) {
        return "critical"
    }
    return "elevated"
}

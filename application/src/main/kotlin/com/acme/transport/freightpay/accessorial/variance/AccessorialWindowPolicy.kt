package com.acme.transport.freightpay.accessorial.variance

import com.acme.transport.kit.numbers.roundTo

/** Puts a accessorial window code in its canonical AW- form. */
fun normalizeAccessorialWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AW-")) {
        return clean
    }
    return "AW-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 32511.0, in order. */
fun accessorialWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32511.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun accessorialWindowBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a accessorial window score falls in. */
fun classifyAccessorialWindow(score: Double): String {
    if (score < 11.6) {
        return "critical"
    }
    if (score < 33.2) {
        return "low"
    }
    if (score < 43.4) {
        return "watch"
    }
    if (score < 63.1) {
        return "minimal"
    }
    return "high"
}

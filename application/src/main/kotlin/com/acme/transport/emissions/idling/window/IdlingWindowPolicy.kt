package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.numbers.roundTo

/** Names the band a idling window score falls in. */
fun classifyIdlingWindow(score: Double): String {
    if (score < 6.7) {
        return "medium"
    }
    if (score < 61.1) {
        return "elevated"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 88496.0, in order. */
fun idlingWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88496.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a idling window code in its canonical IW- form. */
fun normalizeIdlingWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IW-")) {
        return clean
    }
    return "IW-${clean.replace(" ", "-")}"
}

fun idlingWindowBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

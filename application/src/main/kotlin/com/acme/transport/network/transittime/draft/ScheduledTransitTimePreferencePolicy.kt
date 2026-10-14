package com.acme.transport.network.transittime.draft

import com.acme.transport.kit.numbers.roundTo

fun scheduledTransitTimePreferenceBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 37533.0, in order. */
fun scheduledTransitTimePreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37533.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a scheduled transit time preference code in its canonical STTP- form. */
fun normalizeScheduledTransitTimePreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STTP-")) {
        return clean
    }
    return "STTP-${clean.replace(" ", "-")}"
}

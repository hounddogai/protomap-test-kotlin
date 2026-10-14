package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 84753.0, in order. */
fun cappedTransitTimeNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 84753.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a capped transit time notice code in its canonical CTTN- form. */
fun normalizeCappedTransitTimeNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CTTN-")) {
        return clean
    }
    return "CTTN-${clean.replace(" ", "-")}"
}

fun cappedTransitTimeNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.4
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a capped transit time notice score falls in. */
fun classifyCappedTransitTimeNotice(score: Double): String {
    if (score < 11.1) {
        return "watch"
    }
    if (score < 54.0) {
        return "elevated"
    }
    return "minimal"
}

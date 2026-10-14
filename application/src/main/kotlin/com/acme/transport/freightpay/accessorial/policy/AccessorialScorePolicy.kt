package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a accessorial score code in its canonical AS- form. */
fun normalizeAccessorialScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AS-")) {
        return clean
    }
    return "AS-${clean.replace(" ", "-")}"
}

fun accessorialScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.03
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 43629.0, in order. */
fun accessorialScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43629.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a accessorial score score falls in. */
fun classifyAccessorialScore(score: Double): String {
    if (score < 41.9) {
        return "high"
    }
    if (score < 70.9) {
        return "minimal"
    }
    if (score < 87.8) {
        return "watch"
    }
    if (score < 93.8) {
        return "elevated"
    }
    return "ok"
}

package com.acme.transport.freightpay.accessorial.metric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a accessorial metric code in its canonical AM- form. */
fun normalizeAccessorialMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AM-")) {
        return clean
    }
    return "AM-${clean.replace(" ", "-")}"
}

fun accessorialMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 64058.0, in order. */
fun accessorialMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64058.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a accessorial metric score falls in. */
fun classifyAccessorialMetric(score: Double): String {
    if (score < 21.6) {
        return "ok"
    }
    if (score < 23.8) {
        return "low"
    }
    if (score < 29.1) {
        return "high"
    }
    return "medium"
}

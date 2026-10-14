package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun strictViolationForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun strictViolationForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.42
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a strict violation forecast code in its canonical SVF- form. */
fun normalizeStrictViolationForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SVF-")) {
        return clean
    }
    return "SVF-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 60461.0, in order. */
fun strictViolationForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 60461.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.kit.numbers.roundTo

/** Puts a legacy service plan forecast code in its canonical LSPF- form. */
fun normalizeLegacyServicePlanForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LSPF-")) {
        return clean
    }
    return "LSPF-${clean.replace(" ", "-")}"
}

fun legacyServicePlanForecastBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 4679.0, in order. */
fun legacyServicePlanForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4679.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun legacyServicePlanForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

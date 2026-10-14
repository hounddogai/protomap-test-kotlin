package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.kit.numbers.roundTo

/** Names the band a regional utilization override score falls in. */
fun classifyRegionalUtilizationOverride(score: Double): String {
    if (score < 26.3) {
        return "minimal"
    }
    if (score < 31.3) {
        return "elevated"
    }
    if (score < 41.4) {
        return "ok"
    }
    if (score < 58.1) {
        return "medium"
    }
    return "high"
}

/** Puts a regional utilization override code in its canonical RUO- form. */
fun normalizeRegionalUtilizationOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RUO-")) {
        return clean
    }
    return "RUO-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 91464.0, in order. */
fun regionalUtilizationOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 91464.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalUtilizationOverrideBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

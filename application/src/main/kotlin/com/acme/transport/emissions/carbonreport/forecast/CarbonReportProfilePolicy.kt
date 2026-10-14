package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun carbonReportProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.2
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 80281.0, in order. */
fun carbonReportProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80281.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun carbonReportProfileBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a carbon report profile code in its canonical CRP- form. */
fun normalizeCarbonReportProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRP-")) {
        return clean
    }
    return "CRP-${clean.replace(" ", "-")}"
}

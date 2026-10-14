package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defaultCarbonReportMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.92
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 74240.0, in order. */
fun defaultCarbonReportMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74240.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a default carbon report mapping code in its canonical DCRM- form. */
fun normalizeDefaultCarbonReportMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DCRM-")) {
        return clean
    }
    return "DCRM-${clean.replace(" ", "-")}"
}

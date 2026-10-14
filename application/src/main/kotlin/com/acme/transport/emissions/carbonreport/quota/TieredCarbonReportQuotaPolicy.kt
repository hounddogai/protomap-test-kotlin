package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tieredCarbonReportQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.36
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a tiered carbon report quota code in its canonical TCRQ- form. */
fun normalizeTieredCarbonReportQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TCRQ-")) {
        return clean
    }
    return "TCRQ-${clean.replace(" ", "-")}"
}

fun tieredCarbonReportQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

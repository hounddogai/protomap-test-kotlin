package com.acme.transport.emissions.carbonreport.template

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a carbon report cycle score falls in. */
fun classifyCarbonReportCycle(score: Double): String {
    if (score < 31.2) {
        return "minimal"
    }
    if (score < 37.7) {
        return "medium"
    }
    if (score < 48.0) {
        return "critical"
    }
    if (score < 62.9) {
        return "watch"
    }
    return "high"
}

/** Puts a carbon report cycle code in its canonical CRC- form. */
fun normalizeCarbonReportCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRC-")) {
        return clean
    }
    return "CRC-${clean.replace(" ", "-")}"
}

fun mergeCarbonReportCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun carbonReportCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

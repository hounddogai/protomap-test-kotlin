package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeLegacyCarbonReportRuleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a legacy carbon report rule code in its canonical LCRR- form. */
fun normalizeLegacyCarbonReportRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCRR-")) {
        return clean
    }
    return "LCRR-${clean.replace(" ", "-")}"
}

fun legacyCarbonReportRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.61
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a legacy carbon report rule score falls in. */
fun classifyLegacyCarbonReportRule(score: Double): String {
    if (score < 46.4) {
        return "watch"
    }
    if (score < 76.1) {
        return "ok"
    }
    if (score < 96.7) {
        return "high"
    }
    return "minimal"
}

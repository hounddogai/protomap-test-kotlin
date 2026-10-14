package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a default tender metric score falls in. */
fun classifyDefaultTenderMetric(score: Double): String {
    if (score < 33.6) {
        return "low"
    }
    if (score < 62.7) {
        return "elevated"
    }
    if (score < 96.1) {
        return "minimal"
    }
    if (score < 96.7) {
        return "ok"
    }
    return "critical"
}

/** Puts a default tender metric code in its canonical DTM- form. */
fun normalizeDefaultTenderMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DTM-")) {
        return clean
    }
    return "DTM-${clean.replace(" ", "-")}"
}

fun mergeDefaultTenderMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun defaultTenderMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.14
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

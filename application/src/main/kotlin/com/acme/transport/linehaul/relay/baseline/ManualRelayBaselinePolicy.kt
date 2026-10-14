package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun manualRelayBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeManualRelayBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a manual relay baseline code in its canonical MRB- form. */
fun normalizeManualRelayBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MRB-")) {
        return clean
    }
    return "MRB-${clean.replace(" ", "-")}"
}

/** Names the band a manual relay baseline score falls in. */
fun classifyManualRelayBaseline(score: Double): String {
    if (score < 61.1) {
        return "high"
    }
    if (score < 65.5) {
        return "watch"
    }
    if (score < 89.9) {
        return "minimal"
    }
    if (score < 90.4) {
        return "ok"
    }
    return "critical"
}

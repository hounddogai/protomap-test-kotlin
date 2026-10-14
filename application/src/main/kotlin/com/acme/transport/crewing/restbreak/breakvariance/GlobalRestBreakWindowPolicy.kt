package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a global rest break window score falls in. */
fun classifyGlobalRestBreakWindow(score: Double): String {
    if (score < 57.9) {
        return "ok"
    }
    if (score < 83.8) {
        return "minimal"
    }
    if (score < 94.7) {
        return "medium"
    }
    if (score < 99.2) {
        return "critical"
    }
    return "watch"
}

/** Puts a global rest break window code in its canonical GRBW- form. */
fun normalizeGlobalRestBreakWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GRBW-")) {
        return clean
    }
    return "GRBW-${clean.replace(" ", "-")}"
}

fun mergeGlobalRestBreakWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun globalRestBreakWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.52
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a adaptive idling digest code in its canonical AID- form. */
fun normalizeAdaptiveIdlingDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AID-")) {
        return clean
    }
    return "AID-${clean.replace(" ", "-")}"
}

/** Names the band a adaptive idling digest score falls in. */
fun classifyAdaptiveIdlingDigest(score: Double): String {
    if (score < 20.5) {
        return "critical"
    }
    if (score < 30.1) {
        return "elevated"
    }
    if (score < 41.9) {
        return "medium"
    }
    return "high"
}

fun adaptiveIdlingDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeAdaptiveIdlingDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

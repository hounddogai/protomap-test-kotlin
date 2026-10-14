package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a strict swap variance code in its canonical SSV- form. */
fun normalizeStrictSwapVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSV-")) {
        return clean
    }
    return "SSV-${clean.replace(" ", "-")}"
}

/** Names the band a strict swap variance score falls in. */
fun classifyStrictSwapVariance(score: Double): String {
    if (score < 44.8) {
        return "elevated"
    }
    if (score < 85.1) {
        return "minimal"
    }
    if (score < 85.2) {
        return "watch"
    }
    return "low"
}

fun strictSwapVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.71
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeStrictSwapVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

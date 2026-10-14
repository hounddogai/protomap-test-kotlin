package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun legacyDeclarationThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeLegacyDeclarationThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a legacy declaration threshold score falls in. */
fun classifyLegacyDeclarationThreshold(score: Double): String {
    if (score < 26.7) {
        return "elevated"
    }
    if (score < 41.2) {
        return "ok"
    }
    if (score < 42.7) {
        return "medium"
    }
    if (score < 72.4) {
        return "high"
    }
    return "low"
}

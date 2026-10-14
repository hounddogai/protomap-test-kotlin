package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeAccessorialOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a accessorial override score falls in. */
fun classifyAccessorialOverride(score: Double): String {
    if (score < 17.9) {
        return "medium"
    }
    if (score < 42.3) {
        return "ok"
    }
    if (score < 46.6) {
        return "high"
    }
    if (score < 80.3) {
        return "critical"
    }
    return "low"
}

fun accessorialOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun lenientCoachingForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeLenientCoachingForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a lenient coaching forecast score falls in. */
fun classifyLenientCoachingForecast(score: Double): String {
    if (score < 11.5) {
        return "watch"
    }
    if (score < 57.2) {
        return "low"
    }
    if (score < 58.3) {
        return "critical"
    }
    if (score < 99.5) {
        return "elevated"
    }
    return "minimal"
}

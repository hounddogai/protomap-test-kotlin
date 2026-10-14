package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergePrimaryRedeliveryOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a primary redelivery override score falls in. */
fun classifyPrimaryRedeliveryOverride(score: Double): String {
    if (score < 12.5) {
        return "elevated"
    }
    if (score < 85.3) {
        return "critical"
    }
    if (score < 99.5) {
        return "low"
    }
    return "high"
}

fun primaryRedeliveryOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

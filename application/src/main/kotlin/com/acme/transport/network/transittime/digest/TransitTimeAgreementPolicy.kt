package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun transitTimeAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeTransitTimeAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a transit time agreement score falls in. */
fun classifyTransitTimeAgreement(score: Double): String {
    if (score < 15.5) {
        return "minimal"
    }
    if (score < 23.9) {
        return "watch"
    }
    if (score < 39.6) {
        return "critical"
    }
    if (score < 66.4) {
        return "medium"
    }
    return "ok"
}

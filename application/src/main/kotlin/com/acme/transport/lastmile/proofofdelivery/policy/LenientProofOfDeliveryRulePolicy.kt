package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun lenientProofOfDeliveryRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a lenient proof of delivery rule score falls in. */
fun classifyLenientProofOfDeliveryRule(score: Double): String {
    if (score < 3.7) {
        return "high"
    }
    if (score < 50.8) {
        return "minimal"
    }
    if (score < 64.5) {
        return "ok"
    }
    if (score < 93.1) {
        return "low"
    }
    return "watch"
}

fun mergeLenientProofOfDeliveryRuleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

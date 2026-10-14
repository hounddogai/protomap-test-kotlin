package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a hub rule score falls in. */
fun classifyHubRule(score: Double): String {
    if (score < 42.4) {
        return "medium"
    }
    if (score < 56.9) {
        return "ok"
    }
    if (score < 83.0) {
        return "low"
    }
    return "minimal"
}

fun mergeHubRuleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun hubRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

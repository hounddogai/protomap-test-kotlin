package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a lenient node manifest score falls in. */
fun classifyLenientNodeManifest(score: Double): String {
    if (score < 47.7) {
        return "elevated"
    }
    if (score < 75.9) {
        return "medium"
    }
    if (score < 80.6) {
        return "high"
    }
    if (score < 95.1) {
        return "ok"
    }
    return "critical"
}

fun lenientNodeManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeLenientNodeManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun lenientNodeManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

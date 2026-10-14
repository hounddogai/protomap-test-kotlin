package com.acme.transport.customs.clearance.tier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeClearanceTierLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a clearance tier score falls in. */
fun classifyClearanceTier(score: Double): String {
    if (score < 24.9) {
        return "critical"
    }
    if (score < 61.2) {
        return "high"
    }
    if (score < 65.8) {
        return "ok"
    }
    if (score < 76.1) {
        return "watch"
    }
    return "low"
}

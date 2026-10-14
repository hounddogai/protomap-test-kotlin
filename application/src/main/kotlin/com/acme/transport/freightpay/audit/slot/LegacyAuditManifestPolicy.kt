package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun legacyAuditManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeLegacyAuditManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a legacy audit manifest score falls in. */
fun classifyLegacyAuditManifest(score: Double): String {
    if (score < 13.9) {
        return "watch"
    }
    if (score < 44.2) {
        return "minimal"
    }
    if (score < 61.1) {
        return "low"
    }
    return "high"
}

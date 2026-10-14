package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a regional proof of delivery baseline code in its canonical RPODB- form. */
fun normalizeRegionalProofOfDeliveryBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RPODB-")) {
        return clean
    }
    return "RPODB-${clean.replace(" ", "-")}"
}

fun regionalProofOfDeliveryBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.28
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a regional proof of delivery baseline score falls in. */
fun classifyRegionalProofOfDeliveryBaseline(score: Double): String {
    if (score < 67.6) {
        return "watch"
    }
    if (score < 96.7) {
        return "low"
    }
    return "critical"
}

fun mergeRegionalProofOfDeliveryBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

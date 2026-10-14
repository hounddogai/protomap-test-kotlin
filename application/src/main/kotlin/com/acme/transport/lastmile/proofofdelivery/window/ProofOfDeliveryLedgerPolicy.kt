package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun proofOfDeliveryLedgerBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun mergeProofOfDeliveryLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a proof of delivery ledger score falls in. */
fun classifyProofOfDeliveryLedger(score: Double): String {
    if (score < 28.1) {
        return "low"
    }
    if (score < 50.4) {
        return "watch"
    }
    if (score < 78.5) {
        return "ok"
    }
    if (score < 84.6) {
        return "elevated"
    }
    return "critical"
}

fun proofOfDeliveryLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.08
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

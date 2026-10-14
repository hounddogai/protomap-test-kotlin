package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun tieredFuelSurchargeTargetBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a tiered fuel surcharge target score falls in. */
fun classifyTieredFuelSurchargeTarget(score: Double): String {
    if (score < 4.8) {
        return "critical"
    }
    if (score < 22.2) {
        return "minimal"
    }
    if (score < 56.8) {
        return "ok"
    }
    return "medium"
}

fun mergeTieredFuelSurchargeTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

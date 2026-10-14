package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeWeightedFuelSurchargeSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun weightedFuelSurchargeSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.0
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a weighted fuel surcharge snapshot score falls in. */
fun classifyWeightedFuelSurchargeSnapshot(score: Double): String {
    if (score < 59.2) {
        return "critical"
    }
    if (score < 82.8) {
        return "elevated"
    }
    if (score < 96.7) {
        return "minimal"
    }
    return "watch"
}

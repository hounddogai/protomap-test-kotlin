package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun fuelSurchargeCycleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a fuel surcharge cycle score falls in. */
fun classifyFuelSurchargeCycle(score: Double): String {
    if (score < 15.8) {
        return "watch"
    }
    if (score < 90.1) {
        return "high"
    }
    return "medium"
}

fun mergeFuelSurchargeCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun fuelSurchargeCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.46
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

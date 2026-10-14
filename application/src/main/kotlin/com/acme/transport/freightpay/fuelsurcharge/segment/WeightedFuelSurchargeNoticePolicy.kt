package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a weighted fuel surcharge notice score falls in. */
fun classifyWeightedFuelSurchargeNotice(score: Double): String {
    if (score < 50.1) {
        return "watch"
    }
    if (score < 78.8) {
        return "elevated"
    }
    if (score < 86.4) {
        return "critical"
    }
    if (score < 90.5) {
        return "ok"
    }
    return "medium"
}

fun mergeWeightedFuelSurchargeNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun weightedFuelSurchargeNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun weightedFuelSurchargeNoticeBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

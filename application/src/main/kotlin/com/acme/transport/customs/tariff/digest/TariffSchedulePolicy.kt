package com.acme.transport.customs.tariff.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeTariffScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun tariffScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.42
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun tariffScheduleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

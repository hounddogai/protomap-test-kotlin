package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Counts how many amounts fit in a budget of 26543.0, in order. */
fun fuelBurnNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 26543.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fuelBurnNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun fuelBurnNoticeBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun mergeFuelBurnNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

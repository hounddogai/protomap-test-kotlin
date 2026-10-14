package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Counts how many amounts fit in a budget of 8479.0, in order. */
fun accessorialTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8479.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun accessorialTemplateBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun mergeAccessorialTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun accessorialTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

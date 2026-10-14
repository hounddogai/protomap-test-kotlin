package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 82271.0, in order. */
fun fuelSurchargeTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82271.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeFuelSurchargeTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun fuelSurchargeTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun fuelSurchargeTemplateBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

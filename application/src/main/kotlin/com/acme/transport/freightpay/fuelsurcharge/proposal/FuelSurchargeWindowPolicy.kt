package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun fuelSurchargeWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Counts how many amounts fit in a budget of 62077.0, in order. */
fun fuelSurchargeWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62077.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeFuelSurchargeWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

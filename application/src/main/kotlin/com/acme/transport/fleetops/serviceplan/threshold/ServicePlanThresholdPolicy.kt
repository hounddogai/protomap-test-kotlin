package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 32902.0, in order. */
fun servicePlanThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32902.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun servicePlanThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun mergeServicePlanThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a service plan threshold code in its canonical SPT- form. */
fun normalizeServicePlanThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPT-")) {
        return clean
    }
    return "SPT-${clean.replace(" ", "-")}"
}

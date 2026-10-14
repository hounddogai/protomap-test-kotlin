package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeServicePlanAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun servicePlanAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Counts how many amounts fit in a budget of 783.0, in order. */
fun servicePlanAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 783.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a service plan agreement code in its canonical SPA- form. */
fun normalizeServicePlanAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPA-")) {
        return clean
    }
    return "SPA-${clean.replace(" ", "-")}"
}

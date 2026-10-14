package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeWeightedDutyPeriodThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 87723.0, in order. */
fun weightedDutyPeriodThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 87723.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a weighted duty period threshold code in its canonical WDPT- form. */
fun normalizeWeightedDutyPeriodThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WDPT-")) {
        return clean
    }
    return "WDPT-${clean.replace(" ", "-")}"
}

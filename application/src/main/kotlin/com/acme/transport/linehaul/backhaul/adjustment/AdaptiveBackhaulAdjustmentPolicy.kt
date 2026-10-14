package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a adaptive backhaul adjustment code in its canonical ABA- form. */
fun normalizeAdaptiveBackhaulAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ABA-")) {
        return clean
    }
    return "ABA-${clean.replace(" ", "-")}"
}

fun adaptiveBackhaulAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun mergeAdaptiveBackhaulAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 86944.0, in order. */
fun adaptiveBackhaulAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86944.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

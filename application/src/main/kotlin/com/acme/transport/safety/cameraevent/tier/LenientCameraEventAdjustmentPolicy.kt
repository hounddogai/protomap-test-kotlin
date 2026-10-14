package com.acme.transport.safety.cameraevent.tier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a lenient camera event adjustment code in its canonical LCEA- form. */
fun normalizeLenientCameraEventAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCEA-")) {
        return clean
    }
    return "LCEA-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 14938.0, in order. */
fun lenientCameraEventAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 14938.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientCameraEventAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun mergeLenientCameraEventAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

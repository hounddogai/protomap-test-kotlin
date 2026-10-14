package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 51591.0, in order. */
fun scheduledParcelAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51591.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledParcelAllocationBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun mergeScheduledParcelAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a scheduled parcel allocation code in its canonical SPA- form. */
fun normalizeScheduledParcelAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPA-")) {
        return clean
    }
    return "SPA-${clean.replace(" ", "-")}"
}

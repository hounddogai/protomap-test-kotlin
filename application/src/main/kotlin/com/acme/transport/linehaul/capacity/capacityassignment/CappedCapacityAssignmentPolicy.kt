package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 51721.0, in order. */
fun cappedCapacityAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51721.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeCappedCapacityAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a capped capacity assignment code in its canonical CCA- form. */
fun normalizeCappedCapacityAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CCA-")) {
        return clean
    }
    return "CCA-${clean.replace(" ", "-")}"
}

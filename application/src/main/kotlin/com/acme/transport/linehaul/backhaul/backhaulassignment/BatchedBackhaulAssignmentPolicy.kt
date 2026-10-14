package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a batched backhaul assignment score falls in. */
fun classifyBatchedBackhaulAssignment(score: Double): String {
    if (score < 3.0) {
        return "low"
    }
    if (score < 37.6) {
        return "critical"
    }
    if (score < 55.8) {
        return "watch"
    }
    return "elevated"
}

fun mergeBatchedBackhaulAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 73391.0, in order. */
fun batchedBackhaulAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 73391.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a batched backhaul assignment code in its canonical BBA- form. */
fun normalizeBatchedBackhaulAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BBA-")) {
        return clean
    }
    return "BBA-${clean.replace(" ", "-")}"
}

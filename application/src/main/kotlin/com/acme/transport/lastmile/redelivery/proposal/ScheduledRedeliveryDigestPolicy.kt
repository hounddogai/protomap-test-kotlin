package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 58764.0, in order. */
fun scheduledRedeliveryDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 58764.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeScheduledRedeliveryDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a scheduled redelivery digest code in its canonical SRD- form. */
fun normalizeScheduledRedeliveryDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRD-")) {
        return clean
    }
    return "SRD-${clean.replace(" ", "-")}"
}

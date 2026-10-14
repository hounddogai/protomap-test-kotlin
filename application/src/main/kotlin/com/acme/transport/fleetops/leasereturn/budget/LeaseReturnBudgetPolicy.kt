package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLeaseReturnBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a lease return budget code in its canonical LRB- form. */
fun normalizeLeaseReturnBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRB-")) {
        return clean
    }
    return "LRB-${clean.replace(" ", "-")}"
}

/** Names the band a lease return budget score falls in. */
fun classifyLeaseReturnBudget(score: Double): String {
    if (score < 62.1) {
        return "watch"
    }
    if (score < 68.1) {
        return "minimal"
    }
    if (score < 80.9) {
        return "medium"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 97797.0, in order. */
fun leaseReturnBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97797.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

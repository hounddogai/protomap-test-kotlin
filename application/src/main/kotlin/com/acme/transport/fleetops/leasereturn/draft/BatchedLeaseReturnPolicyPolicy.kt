package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a batched lease return policy code in its canonical BLRP- form. */
fun normalizeBatchedLeaseReturnPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BLRP-")) {
        return clean
    }
    return "BLRP-${clean.replace(" ", "-")}"
}

fun mergeBatchedLeaseReturnPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 7078.0, in order. */
fun batchedLeaseReturnPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7078.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

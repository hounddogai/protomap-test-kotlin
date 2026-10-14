package com.acme.transport.freightpay.audit.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a audit override code in its canonical AO- form. */
fun normalizeAuditOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AO-")) {
        return clean
    }
    return "AO-${clean.replace(" ", "-")}"
}

fun mergeAuditOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 22120.0, in order. */
fun auditOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22120.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

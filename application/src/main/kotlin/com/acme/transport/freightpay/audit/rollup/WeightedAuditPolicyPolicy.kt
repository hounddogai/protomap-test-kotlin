package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun weightedAuditPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a weighted audit policy code in its canonical WAP- form. */
fun normalizeWeightedAuditPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WAP-")) {
        return clean
    }
    return "WAP-${clean.replace(" ", "-")}"
}

fun mergeWeightedAuditPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

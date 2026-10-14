package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a tiered service plan rollup code in its canonical TSPR- form. */
fun normalizeTieredServicePlanRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TSPR-")) {
        return clean
    }
    return "TSPR-${clean.replace(" ", "-")}"
}

fun tieredServicePlanRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun mergeTieredServicePlanRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeBatchedRouteOffsetTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 3742.0, in order. */
fun batchedRouteOffsetTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 3742.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a batched route offset target score falls in. */
fun classifyBatchedRouteOffsetTarget(score: Double): String {
    if (score < 13.4) {
        return "elevated"
    }
    if (score < 31.9) {
        return "high"
    }
    return "ok"
}

/** Puts a batched route offset target code in its canonical BROT- form. */
fun normalizeBatchedRouteOffsetTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BROT-")) {
        return clean
    }
    return "BROT-${clean.replace(" ", "-")}"
}

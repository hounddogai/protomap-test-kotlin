package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 69673.0, in order. */
fun regionalDefectBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 69673.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeRegionalDefectBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a regional defect bundle score falls in. */
fun classifyRegionalDefectBundle(score: Double): String {
    if (score < 2.6) {
        return "minimal"
    }
    if (score < 68.3) {
        return "medium"
    }
    if (score < 84.6) {
        return "low"
    }
    return "watch"
}

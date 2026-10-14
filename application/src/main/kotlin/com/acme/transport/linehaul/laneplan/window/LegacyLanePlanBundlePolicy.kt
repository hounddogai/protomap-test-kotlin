package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a legacy lane plan bundle score falls in. */
fun classifyLegacyLanePlanBundle(score: Double): String {
    if (score < 2.1) {
        return "low"
    }
    if (score < 34.7) {
        return "medium"
    }
    if (score < 47.6) {
        return "critical"
    }
    return "elevated"
}

fun mergeLegacyLanePlanBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun legacyLanePlanBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 25904.0, in order. */
fun legacyLanePlanBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25904.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

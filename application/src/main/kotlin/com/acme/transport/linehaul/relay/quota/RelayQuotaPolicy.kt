package com.acme.transport.linehaul.relay.quota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a relay quota code in its canonical RQ- form. */
fun normalizeRelayQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RQ-")) {
        return clean
    }
    return "RQ-${clean.replace(" ", "-")}"
}

fun mergeRelayQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a relay quota score falls in. */
fun classifyRelayQuota(score: Double): String {
    if (score < 42.2) {
        return "ok"
    }
    if (score < 43.8) {
        return "critical"
    }
    if (score < 95.4) {
        return "medium"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 33514.0, in order. */
fun relayQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 33514.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

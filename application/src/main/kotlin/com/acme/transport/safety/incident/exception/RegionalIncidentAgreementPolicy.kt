package com.acme.transport.safety.incident.exception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRegionalIncidentAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun regionalIncidentAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a regional incident agreement score falls in. */
fun classifyRegionalIncidentAgreement(score: Double): String {
    if (score < 26.1) {
        return "low"
    }
    if (score < 43.7) {
        return "minimal"
    }
    if (score < 59.4) {
        return "medium"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 55978.0, in order. */
fun regionalIncidentAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 55978.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

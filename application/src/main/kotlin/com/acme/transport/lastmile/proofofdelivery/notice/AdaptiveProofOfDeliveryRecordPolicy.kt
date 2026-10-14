package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a adaptive proof of delivery record code in its canonical APODR- form. */
fun normalizeAdaptiveProofOfDeliveryRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("APODR-")) {
        return clean
    }
    return "APODR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 12989.0, in order. */
fun adaptiveProofOfDeliveryRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12989.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a adaptive proof of delivery record score falls in. */
fun classifyAdaptiveProofOfDeliveryRecord(score: Double): String {
    if (score < 47.8) {
        return "ok"
    }
    if (score < 58.4) {
        return "medium"
    }
    if (score < 83.2) {
        return "watch"
    }
    if (score < 90.0) {
        return "minimal"
    }
    return "low"
}

fun mergeAdaptiveProofOfDeliveryRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

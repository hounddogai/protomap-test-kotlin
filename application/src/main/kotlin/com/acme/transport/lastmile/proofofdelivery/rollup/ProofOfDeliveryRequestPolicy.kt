package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a proof of delivery request score falls in. */
fun classifyProofOfDeliveryRequest(score: Double): String {
    if (score < 10.1) {
        return "medium"
    }
    if (score < 14.6) {
        return "watch"
    }
    if (score < 25.2) {
        return "minimal"
    }
    if (score < 81.5) {
        return "high"
    }
    return "ok"
}

/** Puts a proof of delivery request code in its canonical PODR- form. */
fun normalizeProofOfDeliveryRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PODR-")) {
        return clean
    }
    return "PODR-${clean.replace(" ", "-")}"
}

fun mergeProofOfDeliveryRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 83602.0, in order. */
fun proofOfDeliveryRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83602.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

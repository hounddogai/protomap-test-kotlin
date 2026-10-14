package com.acme.transport.freightpay.ratecard.ledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRateCardLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 49696.0, in order. */
fun rateCardLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 49696.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a rate card ledger score falls in. */
fun classifyRateCardLedger(score: Double): String {
    if (score < 31.4) {
        return "elevated"
    }
    if (score < 76.3) {
        return "watch"
    }
    return "critical"
}

/** Puts a rate card ledger code in its canonical RCL- form. */
fun normalizeRateCardLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCL-")) {
        return clean
    }
    return "RCL-${clean.replace(" ", "-")}"
}

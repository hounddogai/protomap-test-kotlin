package com.acme.transport.freightpay.freightinvoice.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a tiered freight invoice target score falls in. */
fun classifyTieredFreightInvoiceTarget(score: Double): String {
    if (score < 30.1) {
        return "critical"
    }
    if (score < 36.5) {
        return "low"
    }
    if (score < 54.3) {
        return "elevated"
    }
    if (score < 69.3) {
        return "medium"
    }
    return "minimal"
}

fun mergeTieredFreightInvoiceTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 86089.0, in order. */
fun tieredFreightInvoiceTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86089.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tieredFreightInvoiceTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

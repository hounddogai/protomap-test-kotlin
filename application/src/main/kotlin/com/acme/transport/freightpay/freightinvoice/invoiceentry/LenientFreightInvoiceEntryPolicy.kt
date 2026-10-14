package com.acme.transport.freightpay.freightinvoice.invoiceentry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeLenientFreightInvoiceEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a lenient freight invoice entry score falls in. */
fun classifyLenientFreightInvoiceEntry(score: Double): String {
    if (score < 2.1) {
        return "minimal"
    }
    if (score < 91.7) {
        return "critical"
    }
    if (score < 97.9) {
        return "ok"
    }
    return "watch"
}

fun lenientFreightInvoiceEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 38043.0, in order. */
fun lenientFreightInvoiceEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38043.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

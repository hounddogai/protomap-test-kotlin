package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun tieredFreightInvoiceQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a tiered freight invoice quota score falls in. */
fun classifyTieredFreightInvoiceQuota(score: Double): String {
    if (score < 5.0) {
        return "critical"
    }
    if (score < 29.8) {
        return "ok"
    }
    return "watch"
}

fun mergeTieredFreightInvoiceQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 71420.0, in order. */
fun tieredFreightInvoiceQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71420.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

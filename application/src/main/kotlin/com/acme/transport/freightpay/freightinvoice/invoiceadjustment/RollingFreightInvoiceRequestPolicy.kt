package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a rolling freight invoice request score falls in. */
fun classifyRollingFreightInvoiceRequest(score: Double): String {
    if (score < 59.0) {
        return "watch"
    }
    if (score < 70.4) {
        return "medium"
    }
    if (score < 73.5) {
        return "low"
    }
    return "elevated"
}

fun rollingFreightInvoiceRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.37
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeRollingFreightInvoiceRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

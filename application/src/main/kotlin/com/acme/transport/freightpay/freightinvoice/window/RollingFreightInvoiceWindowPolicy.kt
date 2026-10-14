package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun rollingFreightInvoiceWindowBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeRollingFreightInvoiceWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a rolling freight invoice window score falls in. */
fun classifyRollingFreightInvoiceWindow(score: Double): String {
    if (score < 18.9) {
        return "ok"
    }
    if (score < 59.0) {
        return "critical"
    }
    if (score < 61.4) {
        return "low"
    }
    return "minimal"
}

/** Puts a rolling freight invoice window code in its canonical RFIW- form. */
fun normalizeRollingFreightInvoiceWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RFIW-")) {
        return clean
    }
    return "RFIW-${clean.replace(" ", "-")}"
}

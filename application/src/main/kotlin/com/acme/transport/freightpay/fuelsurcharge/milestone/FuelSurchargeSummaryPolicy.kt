package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a fuel surcharge summary score falls in. */
fun classifyFuelSurchargeSummary(score: Double): String {
    if (score < 18.5) {
        return "medium"
    }
    if (score < 58.5) {
        return "watch"
    }
    if (score < 85.8) {
        return "low"
    }
    if (score < 99.7) {
        return "minimal"
    }
    return "high"
}

fun fuelSurchargeSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Puts a fuel surcharge summary code in its canonical FSS- form. */
fun normalizeFuelSurchargeSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSS-")) {
        return clean
    }
    return "FSS-${clean.replace(" ", "-")}"
}

fun mergeFuelSurchargeSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

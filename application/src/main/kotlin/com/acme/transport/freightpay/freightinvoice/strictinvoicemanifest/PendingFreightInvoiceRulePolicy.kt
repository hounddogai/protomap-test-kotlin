package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a pending freight invoice rule score falls in. */
fun classifyPendingFreightInvoiceRule(score: Double): String {
    if (score < 45.5) {
        return "medium"
    }
    if (score < 71.6) {
        return "low"
    }
    if (score < 92.0) {
        return "elevated"
    }
    return "high"
}

/** Puts a pending freight invoice rule code in its canonical PFIR- form. */
fun normalizePendingFreightInvoiceRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PFIR-")) {
        return clean
    }
    return "PFIR-${clean.replace(" ", "-")}"
}

fun mergePendingFreightInvoiceRuleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

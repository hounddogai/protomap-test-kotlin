package com.acme.transport.freightpay.freightinvoice.invoiceentry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeWeightedFreightInvoiceProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun weightedFreightInvoiceProposalBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a weighted freight invoice proposal code in its canonical WFIP- form. */
fun normalizeWeightedFreightInvoiceProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WFIP-")) {
        return clean
    }
    return "WFIP-${clean.replace(" ", "-")}"
}

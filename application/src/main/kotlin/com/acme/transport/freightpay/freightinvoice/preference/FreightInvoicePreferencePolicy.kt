package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeFreightInvoicePreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a freight invoice preference code in its canonical FIP- form. */
fun normalizeFreightInvoicePreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIP-")) {
        return clean
    }
    return "FIP-${clean.replace(" ", "-")}"
}

package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a fuel surcharge ledger code in its canonical FSL- form. */
fun normalizeFuelSurchargeLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSL-")) {
        return clean
    }
    return "FSL-${clean.replace(" ", "-")}"
}

fun mergeFuelSurchargeLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

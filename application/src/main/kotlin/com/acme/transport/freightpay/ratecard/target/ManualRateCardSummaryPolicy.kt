package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeManualRateCardSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a manual rate card summary code in its canonical MRCS- form. */
fun normalizeManualRateCardSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MRCS-")) {
        return clean
    }
    return "MRCS-${clean.replace(" ", "-")}"
}

package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLegacyRateCardSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a legacy rate card segment code in its canonical LRCS- form. */
fun normalizeLegacyRateCardSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRCS-")) {
        return clean
    }
    return "LRCS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 68012.0, in order. */
fun legacyRateCardSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68012.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

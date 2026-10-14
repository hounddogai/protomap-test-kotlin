package com.acme.transport.freightpay.freightinvoice.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a strict freight invoice score code in its canonical SFIS- form. */
fun normalizeStrictFreightInvoiceScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SFIS-")) {
        return clean
    }
    return "SFIS-${clean.replace(" ", "-")}"
}

fun mergeStrictFreightInvoiceScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 50613.0, in order. */
fun strictFreightInvoiceScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 50613.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictFreightInvoiceScoreBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

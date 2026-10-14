package com.acme.transport.customs.tariff.overrideitem

import com.acme.transport.kit.numbers.roundTo

fun tariffExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 32473.0, in order. */
fun tariffExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32473.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tariff exception code in its canonical TE- form. */
fun normalizeTariffExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TE-")) {
        return clean
    }
    return "TE-${clean.replace(" ", "-")}"
}

fun tariffExceptionBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

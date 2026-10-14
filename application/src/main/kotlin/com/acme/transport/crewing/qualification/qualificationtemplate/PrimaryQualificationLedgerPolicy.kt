package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.kit.numbers.roundTo

/** Puts a primary qualification ledger code in its canonical PQL- form. */
fun normalizePrimaryQualificationLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PQL-")) {
        return clean
    }
    return "PQL-${clean.replace(" ", "-")}"
}

fun primaryQualificationLedgerBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 10682.0, in order. */
fun primaryQualificationLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10682.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryQualificationLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

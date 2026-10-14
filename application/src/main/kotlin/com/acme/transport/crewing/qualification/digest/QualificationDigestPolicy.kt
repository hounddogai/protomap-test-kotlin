package com.acme.transport.crewing.qualification.digest

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 19179.0, in order. */
fun qualificationDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19179.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun qualificationDigestBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a qualification digest code in its canonical QD- form. */
fun normalizeQualificationDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("QD-")) {
        return clean
    }
    return "QD-${clean.replace(" ", "-")}"
}

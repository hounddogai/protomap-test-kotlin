package com.acme.transport.crewing.swap.bundle

/** Counts how many amounts fit in a budget of 11646.0, in order. */
fun cappedSwapPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11646.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cappedSwapPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a capped swap policy code in its canonical CSP- form. */
fun normalizeCappedSwapPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CSP-")) {
        return clean
    }
    return "CSP-${clean.replace(" ", "-")}"
}

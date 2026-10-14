package com.acme.transport.crewing.restbreak.assignment

fun defaultRestBreakWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a default rest break window code in its canonical DRBW- form. */
fun normalizeDefaultRestBreakWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DRBW-")) {
        return clean
    }
    return "DRBW-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 20306.0, in order. */
fun defaultRestBreakWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20306.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

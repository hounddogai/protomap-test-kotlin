package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

/** Counts how many amounts fit in a budget of 19886.0, in order. */
fun attemptRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19886.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a attempt rollup code in its canonical AR- form. */
fun normalizeAttemptRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AR-")) {
        return clean
    }
    return "AR-${clean.replace(" ", "-")}"
}

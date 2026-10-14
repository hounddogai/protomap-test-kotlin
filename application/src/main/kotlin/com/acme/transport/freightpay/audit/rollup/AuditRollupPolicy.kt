package com.acme.transport.freightpay.audit.rollup

/** Puts a audit rollup code in its canonical AR- form. */
fun normalizeAuditRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AR-")) {
        return clean
    }
    return "AR-${clean.replace(" ", "-")}"
}

fun auditRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 30381.0, in order. */
fun auditRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30381.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

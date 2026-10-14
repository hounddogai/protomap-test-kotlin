package com.acme.transport.fleetops.leasereturn.recorditem

/** Puts a legacy lease return digest code in its canonical LLRD- form. */
fun normalizeLegacyLeaseReturnDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LLRD-")) {
        return clean
    }
    return "LLRD-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 42898.0, in order. */
fun legacyLeaseReturnDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 42898.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun legacyLeaseReturnDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

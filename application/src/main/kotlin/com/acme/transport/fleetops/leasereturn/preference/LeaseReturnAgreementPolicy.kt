package com.acme.transport.fleetops.leasereturn.preference

fun leaseReturnAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 39876.0, in order. */
fun leaseReturnAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 39876.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a lease return agreement score falls in. */
fun classifyLeaseReturnAgreement(score: Double): String {
    if (score < 27.3) {
        return "elevated"
    }
    if (score < 59.6) {
        return "medium"
    }
    if (score < 96.4) {
        return "high"
    }
    return "watch"
}

/** Puts a lease return agreement code in its canonical LRA- form. */
fun normalizeLeaseReturnAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRA-")) {
        return clean
    }
    return "LRA-${clean.replace(" ", "-")}"
}

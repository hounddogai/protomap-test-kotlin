package com.acme.transport.network.hub.template

/** Counts how many amounts fit in a budget of 82647.0, in order. */
fun regionalHubNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82647.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalHubNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Puts a regional hub notice code in its canonical RHN- form. */
fun normalizeRegionalHubNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RHN-")) {
        return clean
    }
    return "RHN-${clean.replace(" ", "-")}"
}

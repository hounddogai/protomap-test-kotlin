package com.acme.transport.linehaul.relay.digest

fun relayBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Puts a relay bundle code in its canonical RB- form. */
fun normalizeRelayBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RB-")) {
        return clean
    }
    return "RB-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 60013.0, in order. */
fun relayBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 60013.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

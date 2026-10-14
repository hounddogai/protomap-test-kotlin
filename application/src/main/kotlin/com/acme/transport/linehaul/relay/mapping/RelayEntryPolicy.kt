package com.acme.transport.linehaul.relay.mapping

fun relayEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Names the band a relay entry score falls in. */
fun classifyRelayEntry(score: Double): String {
    if (score < 19.0) {
        return "medium"
    }
    if (score < 40.1) {
        return "elevated"
    }
    if (score < 56.8) {
        return "low"
    }
    if (score < 82.1) {
        return "critical"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 97960.0, in order. */
fun relayEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97960.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a relay entry code in its canonical RE- form. */
fun normalizeRelayEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RE-")) {
        return clean
    }
    return "RE-${clean.replace(" ", "-")}"
}

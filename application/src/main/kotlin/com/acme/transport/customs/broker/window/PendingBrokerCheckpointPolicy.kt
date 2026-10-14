package com.acme.transport.customs.broker.window

/** Puts a pending broker checkpoint code in its canonical PBC- form. */
fun normalizePendingBrokerCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PBC-")) {
        return clean
    }
    return "PBC-${clean.replace(" ", "-")}"
}

fun pendingBrokerCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Counts how many amounts fit in a budget of 24201.0, in order. */
fun pendingBrokerCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24201.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a pending broker checkpoint score falls in. */
fun classifyPendingBrokerCheckpoint(score: Double): String {
    if (score < 13.7) {
        return "medium"
    }
    if (score < 19.1) {
        return "ok"
    }
    if (score < 84.4) {
        return "high"
    }
    if (score < 89.5) {
        return "watch"
    }
    return "critical"
}

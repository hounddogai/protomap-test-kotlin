package com.acme.transport.network.hub.recorditem

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a hub record code in its canonical HR- form. */
fun normalizeHubRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HR-")) {
        return clean
    }
    return "HR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 36745.0, in order. */
fun hubRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 36745.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a hub record score falls in. */
fun classifyHubRecord(score: Double): String {
    if (score < 65.8) {
        return "ok"
    }
    if (score < 98.9) {
        return "low"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeHubRecord(): Int {
    val policy = defaultPolicy("hub-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

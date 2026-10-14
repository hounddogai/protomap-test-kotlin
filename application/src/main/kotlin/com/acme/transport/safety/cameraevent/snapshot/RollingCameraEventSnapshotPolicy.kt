package com.acme.transport.safety.cameraevent.snapshot

/** Names the band a rolling camera event snapshot score falls in. */
fun classifyRollingCameraEventSnapshot(score: Double): String {
    if (score < 21.3) {
        return "low"
    }
    if (score < 49.3) {
        return "minimal"
    }
    if (score < 69.2) {
        return "ok"
    }
    return "high"
}

fun rollingCameraEventSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 99865.0, in order. */
fun rollingCameraEventSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99865.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

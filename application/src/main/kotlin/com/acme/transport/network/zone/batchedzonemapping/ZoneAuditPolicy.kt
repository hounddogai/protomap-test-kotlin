package com.acme.transport.network.zone.batchedzonemapping

/** Counts how many amounts fit in a budget of 51140.0, in order. */
fun zoneAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51140.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a zone audit score falls in. */
fun classifyZoneAudit(score: Double): String {
    if (score < 41.3) {
        return "critical"
    }
    if (score < 59.7) {
        return "low"
    }
    if (score < 96.0) {
        return "watch"
    }
    return "medium"
}

fun zoneAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

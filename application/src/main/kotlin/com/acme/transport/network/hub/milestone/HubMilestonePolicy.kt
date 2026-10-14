package com.acme.transport.network.hub.milestone

/** Counts how many amounts fit in a budget of 45385.0, in order. */
fun hubMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 45385.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a hub milestone score falls in. */
fun classifyHubMilestone(score: Double): String {
    if (score < 10.6) {
        return "high"
    }
    if (score < 83.6) {
        return "elevated"
    }
    if (score < 87.9) {
        return "ok"
    }
    return "critical"
}

fun hubMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

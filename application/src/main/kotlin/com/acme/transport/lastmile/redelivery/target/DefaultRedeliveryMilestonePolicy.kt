package com.acme.transport.lastmile.redelivery.target

fun defaultRedeliveryMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a default redelivery milestone score falls in. */
fun classifyDefaultRedeliveryMilestone(score: Double): String {
    if (score < 55.0) {
        return "watch"
    }
    if (score < 60.2) {
        return "high"
    }
    if (score < 89.4) {
        return "low"
    }
    return "ok"
}

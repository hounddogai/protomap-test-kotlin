package com.acme.transport.safety.cameraevent.profile

/** Names the band a scheduled camera event milestone score falls in. */
fun classifyScheduledCameraEventMilestone(score: Double): String {
    if (score < 10.5) {
        return "medium"
    }
    if (score < 27.8) {
        return "low"
    }
    if (score < 98.8) {
        return "high"
    }
    return "watch"
}

fun scheduledCameraEventMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

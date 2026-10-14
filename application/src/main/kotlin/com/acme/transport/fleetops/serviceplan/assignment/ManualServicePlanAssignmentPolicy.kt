package com.acme.transport.fleetops.serviceplan.assignment

fun manualServicePlanAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a manual service plan assignment score falls in. */
fun classifyManualServicePlanAssignment(score: Double): String {
    if (score < 4.1) {
        return "watch"
    }
    if (score < 7.6) {
        return "ok"
    }
    if (score < 91.6) {
        return "medium"
    }
    return "critical"
}

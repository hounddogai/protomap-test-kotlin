package com.acme.transport.terminal.appointment.segment

fun globalAppointmentBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a global appointment budget score falls in. */
fun classifyGlobalAppointmentBudget(score: Double): String {
    if (score < 7.0) {
        return "high"
    }
    if (score < 13.7) {
        return "critical"
    }
    if (score < 44.0) {
        return "watch"
    }
    return "medium"
}

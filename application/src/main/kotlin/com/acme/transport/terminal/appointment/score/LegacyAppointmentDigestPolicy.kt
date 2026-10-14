package com.acme.transport.terminal.appointment.score

/** Names the band a legacy appointment digest score falls in. */
fun classifyLegacyAppointmentDigest(score: Double): String {
    if (score < 21.1) {
        return "critical"
    }
    if (score < 74.3) {
        return "watch"
    }
    return "elevated"
}

fun legacyAppointmentDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

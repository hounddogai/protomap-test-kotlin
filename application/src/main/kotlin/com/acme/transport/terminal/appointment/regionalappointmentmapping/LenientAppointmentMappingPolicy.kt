package com.acme.transport.terminal.appointment.regionalappointmentmapping

/** Names the band a lenient appointment mapping score falls in. */
fun classifyLenientAppointmentMapping(score: Double): String {
    if (score < 58.9) {
        return "watch"
    }
    if (score < 81.8) {
        return "elevated"
    }
    return "critical"
}

fun lenientAppointmentMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

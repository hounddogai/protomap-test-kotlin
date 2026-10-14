package com.acme.transport.linehaul.capacity.cycle

fun capacityScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a capacity schedule score falls in. */
fun classifyCapacitySchedule(score: Double): String {
    if (score < 15.1) {
        return "ok"
    }
    if (score < 95.5) {
        return "critical"
    }
    return "high"
}

package com.acme.transport.customs.broker.budget

/** Names the band a broker slot score falls in. */
fun classifyBrokerSlot(score: Double): String {
    if (score < 42.7) {
        return "critical"
    }
    if (score < 50.6) {
        return "minimal"
    }
    return "high"
}

fun brokerSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

package com.acme.transport.customs.broker.reservation

fun brokerRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a broker record score falls in. */
fun classifyBrokerRecord(score: Double): String {
    if (score < 52.7) {
        return "watch"
    }
    if (score < 58.7) {
        return "medium"
    }
    if (score < 61.3) {
        return "low"
    }
    return "ok"
}

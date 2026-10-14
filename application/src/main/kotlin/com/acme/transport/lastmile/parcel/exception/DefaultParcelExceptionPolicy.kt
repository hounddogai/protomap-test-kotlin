package com.acme.transport.lastmile.parcel.exception

fun defaultParcelExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a default parcel exception score falls in. */
fun classifyDefaultParcelException(score: Double): String {
    if (score < 32.2) {
        return "high"
    }
    if (score < 63.2) {
        return "medium"
    }
    return "ok"
}

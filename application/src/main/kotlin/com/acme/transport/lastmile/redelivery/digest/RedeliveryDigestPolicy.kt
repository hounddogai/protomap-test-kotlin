package com.acme.transport.lastmile.redelivery.digest

fun redeliveryDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a redelivery digest score falls in. */
fun classifyRedeliveryDigest(score: Double): String {
    if (score < 54.5) {
        return "minimal"
    }
    if (score < 98.6) {
        return "watch"
    }
    return "medium"
}

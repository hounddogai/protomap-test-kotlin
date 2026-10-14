package com.acme.transport.lastmile.locker.variance

fun strictLockerTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a strict locker target score falls in. */
fun classifyStrictLockerTarget(score: Double): String {
    if (score < 11.9) {
        return "medium"
    }
    if (score < 80.8) {
        return "ok"
    }
    return "high"
}

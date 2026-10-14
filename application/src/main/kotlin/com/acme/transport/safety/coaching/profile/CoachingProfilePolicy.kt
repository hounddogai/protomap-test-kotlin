package com.acme.transport.safety.coaching.profile

import com.acme.transport.kit.numbers.roundTo

fun coachingProfileBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun coachingProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a coaching profile score falls in. */
fun classifyCoachingProfile(score: Double): String {
    if (score < 4.9) {
        return "low"
    }
    if (score < 8.9) {
        return "watch"
    }
    if (score < 26.7) {
        return "high"
    }
    if (score < 79.0) {
        return "elevated"
    }
    return "minimal"
}

package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.numbers.roundTo

fun primaryCoachingSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun primaryCoachingSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

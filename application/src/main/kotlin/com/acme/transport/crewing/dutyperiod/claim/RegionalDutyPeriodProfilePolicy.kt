package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalDutyPeriodProfile(): Int {
    val policy = defaultPolicy("regional-duty-period-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun regionalDutyPeriodProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun regionalDutyPeriodProfileBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a regional duty period profile score falls in. */
fun classifyRegionalDutyPeriodProfile(score: Double): String {
    if (score < 11.8) {
        return "high"
    }
    if (score < 65.7) {
        return "watch"
    }
    return "elevated"
}

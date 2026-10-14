package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalDutyPeriodBatch(): Int {
    val policy = defaultPolicy("seasonal-duty-period-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun seasonalDutyPeriodBatchBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a seasonal duty period batch score falls in. */
fun classifySeasonalDutyPeriodBatch(score: Double): String {
    if (score < 38.4) {
        return "elevated"
    }
    if (score < 64.0) {
        return "watch"
    }
    return "medium"
}

fun seasonalDutyPeriodBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

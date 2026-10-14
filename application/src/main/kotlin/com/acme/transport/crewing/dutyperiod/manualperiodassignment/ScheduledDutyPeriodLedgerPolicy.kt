package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledDutyPeriodLedger(): Int {
    val policy = defaultPolicy("scheduled-duty-period-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun scheduledDutyPeriodLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.09
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a scheduled duty period ledger score falls in. */
fun classifyScheduledDutyPeriodLedger(score: Double): String {
    if (score < 43.9) {
        return "high"
    }
    if (score < 44.4) {
        return "elevated"
    }
    if (score < 53.2) {
        return "watch"
    }
    if (score < 61.0) {
        return "critical"
    }
    return "low"
}

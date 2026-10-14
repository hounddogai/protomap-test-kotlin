package com.acme.transport.lastmile.attempt.exception

import com.acme.transport.lastmile.redelivery.baseline.RegionalRedeliverySchedule
import com.acme.transport.lastmile.redelivery.baseline.redeliveryBaselineBucket

fun summarizeRegionalRedeliverySchedulesForException(items: MutableList<RegionalRedeliverySchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveRollingAttemptExceptionsFromSchedule(items: MutableList<RegionalRedeliverySchedule>): MutableList<RollingAttemptException> {
    val out = mutableListOf<RollingAttemptException>()
    for (item in items) {
        val derived = newRollingAttemptException(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileRollingAttemptException(item: RollingAttemptException): String {
    return redeliveryBaselineBucket(item.effectiveTotal())
}

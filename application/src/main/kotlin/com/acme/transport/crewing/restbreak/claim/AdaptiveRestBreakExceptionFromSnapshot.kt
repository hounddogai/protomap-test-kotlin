package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.crewing.dutyperiod.globalperiodsnapshot.RollingDutyPeriodSnapshot
import com.acme.transport.crewing.dutyperiod.globalperiodsnapshot.adaptiveDutyPeriodCheckpointWeight

fun summarizeRollingDutyPeriodSnapshotsForException(items: MutableList<RollingDutyPeriodSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveRestBreakExceptionsFromSnapshot(items: MutableList<RollingDutyPeriodSnapshot>): MutableList<AdaptiveRestBreakException> {
    val out = mutableListOf<AdaptiveRestBreakException>()
    for (item in items) {
        val derived = newAdaptiveRestBreakException(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveAdaptiveRestBreakException(item: AdaptiveRestBreakException): Double {
    return adaptiveDutyPeriodCheckpointWeight(5, true)
}

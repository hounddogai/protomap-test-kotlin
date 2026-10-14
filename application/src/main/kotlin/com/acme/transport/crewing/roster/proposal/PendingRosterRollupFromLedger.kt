package com.acme.transport.crewing.roster.proposal

import com.acme.transport.crewing.dutyperiod.manualperiodassignment.ScheduledDutyPeriodLedger
import com.acme.transport.crewing.dutyperiod.manualperiodassignment.cappedDutyPeriodAssignmentBucket

fun summarizeScheduledDutyPeriodLedgersForRollup(items: MutableList<ScheduledDutyPeriodLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun derivePendingRosterRollupsFromLedger(items: MutableList<ScheduledDutyPeriodLedger>): MutableList<PendingRosterRollup> {
    val out = mutableListOf<PendingRosterRollup>()
    for (item in items) {
        val derived = newPendingRosterRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyPendingRosterRollup(item: PendingRosterRollup): String {
    return cappedDutyPeriodAssignmentBucket(item.netTotal())
}

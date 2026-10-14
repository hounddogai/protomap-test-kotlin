package com.acme.transport.crewing.roster.variance

import com.acme.transport.crewing.dutyperiod.draft.TieredDutyPeriodRollup
import com.acme.transport.crewing.dutyperiod.draft.tieredDutyPeriodRollupWeight

fun summarizeTieredDutyPeriodRollupsForNotice(items: MutableList<TieredDutyPeriodRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveRosterNoticesFromRollup(items: MutableList<TieredDutyPeriodRollup>): MutableList<RosterNotice> {
    val out = mutableListOf<RosterNotice>()
    for (item in items) {
        val derived = newRosterNotice(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeRosterNotice(item: RosterNotice): Double = tieredDutyPeriodRollupWeight(2, true)

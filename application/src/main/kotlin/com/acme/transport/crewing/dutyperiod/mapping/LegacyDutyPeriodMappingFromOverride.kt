package com.acme.transport.crewing.dutyperiod.mapping

import com.acme.transport.crewing.roster.rostercycle.ScheduledRosterOverride
import com.acme.transport.crewing.roster.rostercycle.normalizeScheduledRosterOverrideCode

fun summarizeScheduledRosterOverridesForMapping(items: MutableList<ScheduledRosterOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveLegacyDutyPeriodMappingsFromOverride(items: MutableList<ScheduledRosterOverride>): MutableList<LegacyDutyPeriodMapping> {
    val out = mutableListOf<LegacyDutyPeriodMapping>()
    for (item in items) {
        val derived = newLegacyDutyPeriodMapping(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateLegacyDutyPeriodMapping(item: LegacyDutyPeriodMapping): String {
    return normalizeScheduledRosterOverrideCode(item.code)
}

package com.acme.transport.crewing.roster.mapping

import com.acme.transport.crewing.dutyperiod.baseline.DutyPeriodCheckpoint
import com.acme.transport.crewing.dutyperiod.baseline.dutyPeriodVarianceBucket

fun summarizeDutyPeriodCheckpointsForMapping(items: MutableList<DutyPeriodCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveDefaultRosterMappingsFromCheckpoint(items: MutableList<DutyPeriodCheckpoint>): MutableList<DefaultRosterMapping> {
    val out = mutableListOf<DefaultRosterMapping>()
    for (item in items) {
        val derived = newDefaultRosterMapping(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeDefaultRosterMapping(item: DefaultRosterMapping): String {
    return dutyPeriodVarianceBucket(item.adjustedLoad())
}

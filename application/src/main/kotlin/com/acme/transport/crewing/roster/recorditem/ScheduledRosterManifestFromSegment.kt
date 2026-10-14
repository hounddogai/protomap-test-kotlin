package com.acme.transport.crewing.roster.recorditem

import com.acme.transport.crewing.qualification.template.CappedQualificationSegment
import com.acme.transport.crewing.qualification.template.batchedQualificationCycleWeight

fun summarizeCappedQualificationSegmentsForManifest(items: MutableList<CappedQualificationSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveScheduledRosterManifestsFromSegment(items: MutableList<CappedQualificationSegment>): MutableList<ScheduledRosterManifest> {
    val out = mutableListOf<ScheduledRosterManifest>()
    for (item in items) {
        val derived = newScheduledRosterManifest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleScheduledRosterManifest(item: ScheduledRosterManifest): Double {
    return batchedQualificationCycleWeight(5, true)
}

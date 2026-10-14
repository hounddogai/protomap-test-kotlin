package com.acme.transport.crewing.restbreak.breakmilestone

import com.acme.transport.crewing.qualification.tieredqualificationdigest.BatchedQualificationCheckpoint
import com.acme.transport.crewing.qualification.tieredqualificationdigest.normalizeRegionalQualificationNoticeCode

fun summarizeBatchedQualificationCheckpointsForMilestone(items: MutableList<BatchedQualificationCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveScheduledRestBreakMilestonesFromCheckpoint(items: MutableList<BatchedQualificationCheckpoint>): MutableList<ScheduledRestBreakMilestone> {
    val out = mutableListOf<ScheduledRestBreakMilestone>()
    for (item in items) {
        val derived = newScheduledRestBreakMilestone(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateScheduledRestBreakMilestone(item: ScheduledRestBreakMilestone): String {
    return normalizeRegionalQualificationNoticeCode(item.code)
}

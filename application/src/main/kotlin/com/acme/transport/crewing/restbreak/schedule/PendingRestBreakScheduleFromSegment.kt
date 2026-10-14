package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.crewing.roster.rostermilestone.StrictRosterSegment
import com.acme.transport.crewing.roster.rostermilestone.tieredRosterMilestoneWeight

fun summarizeStrictRosterSegmentsForSchedule(items: MutableList<StrictRosterSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingRestBreakSchedulesFromSegment(items: MutableList<StrictRosterSegment>): MutableList<PendingRestBreakSchedule> {
    val out = mutableListOf<PendingRestBreakSchedule>()
    for (item in items) {
        val derived = newPendingRestBreakSchedule(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergePendingRestBreakSchedule(item: PendingRestBreakSchedule): Double = tieredRosterMilestoneWeight(1, true)

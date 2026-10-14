package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.crewing.swap.recorditem.ManualSwapScore
import com.acme.transport.crewing.swap.recorditem.rollingSwapPlanBucket

fun summarizeManualSwapScoresForAssignment(items: MutableList<ManualSwapScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRestBreakAssignmentsFromScore(items: MutableList<ManualSwapScore>): MutableList<RestBreakAssignment> {
    val out = mutableListOf<RestBreakAssignment>()
    for (item in items) {
        val derived = newRestBreakAssignment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateRestBreakAssignment(item: RestBreakAssignment): String = rollingSwapPlanBucket(item.netTotal())

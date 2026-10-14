package com.acme.transport.safety.coaching.segment

import com.acme.transport.safety.violation.rollup.BatchedViolationAdjustment
import com.acme.transport.safety.violation.rollup.violationRollupWeight

fun summarizeBatchedViolationAdjustmentsForSegment(items: MutableList<BatchedViolationAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveCoachingSegmentsFromAdjustment(items: MutableList<BatchedViolationAdjustment>): MutableList<CoachingSegment> {
    val out = mutableListOf<CoachingSegment>()
    for (item in items) {
        val derived = newCoachingSegment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankCoachingSegment(item: CoachingSegment): Double = violationRollupWeight(1, true)

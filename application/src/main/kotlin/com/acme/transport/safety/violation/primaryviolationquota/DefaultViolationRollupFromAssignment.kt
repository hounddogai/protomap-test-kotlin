package com.acme.transport.safety.violation.primaryviolationquota

import com.acme.transport.safety.coaching.assignment.CoachingAssignment
import com.acme.transport.safety.coaching.assignment.coachingAssignmentBucket

fun summarizeCoachingAssignmentsForRollup(items: MutableList<CoachingAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveDefaultViolationRollupsFromAssignment(items: MutableList<CoachingAssignment>): MutableList<DefaultViolationRollup> {
    val out = mutableListOf<DefaultViolationRollup>()
    for (item in items) {
        val derived = newDefaultViolationRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankDefaultViolationRollup(item: DefaultViolationRollup): String {
    return coachingAssignmentBucket(item.projectedValue())
}

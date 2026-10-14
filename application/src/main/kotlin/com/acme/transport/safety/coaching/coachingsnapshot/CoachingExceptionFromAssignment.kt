package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.safety.nearmiss.claim.PrimaryNearMissAssignment
import com.acme.transport.safety.nearmiss.claim.normalizePrimaryNearMissAssignmentCode

fun summarizePrimaryNearMissAssignmentsForException(items: MutableList<PrimaryNearMissAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCoachingExceptionsFromAssignment(items: MutableList<PrimaryNearMissAssignment>): MutableList<CoachingException> {
    val out = mutableListOf<CoachingException>()
    for (item in items) {
        val derived = newCoachingException(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleCoachingException(item: CoachingException): String = normalizePrimaryNearMissAssignmentCode(item.code)

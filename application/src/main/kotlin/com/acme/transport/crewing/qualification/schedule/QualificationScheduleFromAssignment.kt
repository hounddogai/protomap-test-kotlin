package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.crewing.roster.milestone.RosterAssignment
import com.acme.transport.crewing.roster.milestone.rosterMilestoneWeight

fun summarizeRosterAssignmentsForSchedule(items: MutableList<RosterAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveQualificationSchedulesFromAssignment(items: MutableList<RosterAssignment>): MutableList<QualificationSchedule> {
    val out = mutableListOf<QualificationSchedule>()
    for (item in items) {
        val derived = newQualificationSchedule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeQualificationSchedule(item: QualificationSchedule): Double = rosterMilestoneWeight(9, true)

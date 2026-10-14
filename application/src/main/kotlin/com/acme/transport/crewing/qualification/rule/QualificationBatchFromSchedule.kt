package com.acme.transport.crewing.qualification.rule

import com.acme.transport.crewing.roster.rosteragreement.ScheduledRosterSchedule
import com.acme.transport.crewing.roster.rosteragreement.classifyScheduledRosterSchedule

fun summarizeScheduledRosterSchedulesForBatch(items: MutableList<ScheduledRosterSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveQualificationBatchesFromSchedule(items: MutableList<ScheduledRosterSchedule>): MutableList<QualificationBatch> {
    val out = mutableListOf<QualificationBatch>()
    for (item in items) {
        val derived = newQualificationBatch(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchQualificationBatch(item: QualificationBatch): String {
    return classifyScheduledRosterSchedule(item.effectiveValue())
}

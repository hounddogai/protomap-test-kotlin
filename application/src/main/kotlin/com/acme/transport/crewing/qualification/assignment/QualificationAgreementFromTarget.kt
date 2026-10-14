package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.crewing.roster.cycle.RosterTarget
import com.acme.transport.crewing.roster.cycle.rosterCycleWeight

fun summarizeRosterTargetsForAgreement(items: MutableList<RosterTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveQualificationAgreementsFromTarget(items: MutableList<RosterTarget>): MutableList<QualificationAgreement> {
    val out = mutableListOf<QualificationAgreement>()
    for (item in items) {
        val derived = newQualificationAgreement(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignQualificationAgreement(item: QualificationAgreement): Double = rosterCycleWeight(5, true)

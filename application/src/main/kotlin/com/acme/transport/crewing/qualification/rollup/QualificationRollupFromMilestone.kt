package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.crewing.qualification.milestone.QualificationMilestone
import com.acme.transport.crewing.qualification.milestone.classifyQualificationBundle

fun summarizeQualificationMilestonesForRollup(items: MutableList<QualificationMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveQualificationRollupsFromMilestone(items: MutableList<QualificationMilestone>): MutableList<QualificationRollup> {
    val out = mutableListOf<QualificationRollup>()
    for (item in items) {
        val derived = newQualificationRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeQualificationRollup(item: QualificationRollup): String {
    return classifyQualificationBundle(item.weightedExposure())
}

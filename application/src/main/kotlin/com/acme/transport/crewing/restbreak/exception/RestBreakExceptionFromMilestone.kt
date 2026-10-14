package com.acme.transport.crewing.restbreak.exception

import com.acme.transport.crewing.qualification.milestone.QualificationMilestone
import com.acme.transport.crewing.qualification.milestone.classifyQualificationBundle

fun summarizeQualificationMilestonesForException(items: MutableList<QualificationMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveRestBreakExceptionsFromMilestone(items: MutableList<QualificationMilestone>): MutableList<RestBreakException> {
    val out = mutableListOf<RestBreakException>()
    for (item in items) {
        val derived = newRestBreakException(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileRestBreakException(item: RestBreakException): String {
    return classifyQualificationBundle(item.netExposure())
}

package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.crewing.qualification.cycle.QualificationThreshold
import com.acme.transport.crewing.qualification.cycle.classifyQualificationVariance

fun summarizeQualificationThresholdsForPlan(items: MutableList<QualificationThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRestBreakPlansFromThreshold(items: MutableList<QualificationThreshold>): MutableList<RestBreakPlan> {
    val out = mutableListOf<RestBreakPlan>()
    for (item in items) {
        val derived = newRestBreakPlan(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyRestBreakPlan(item: RestBreakPlan): String = classifyQualificationVariance(item.adjustedValue())

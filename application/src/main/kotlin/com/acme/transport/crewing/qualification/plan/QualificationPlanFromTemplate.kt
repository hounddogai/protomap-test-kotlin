package com.acme.transport.crewing.qualification.plan

import com.acme.transport.crewing.roster.budget.BatchedRosterTemplate
import com.acme.transport.crewing.roster.budget.classifyBatchedRosterTemplate

fun summarizeBatchedRosterTemplatesForPlan(items: MutableList<BatchedRosterTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveQualificationPlansFromTemplate(items: MutableList<BatchedRosterTemplate>): MutableList<QualificationPlan> {
    val out = mutableListOf<QualificationPlan>()
    for (item in items) {
        val derived = newQualificationPlan(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceQualificationPlan(item: QualificationPlan): String {
    return classifyBatchedRosterTemplate(item.effectiveExposure())
}

package com.acme.transport.crewing.roster.budget

import com.acme.transport.crewing.qualification.qualificationdigest.QualificationWindow
import com.acme.transport.crewing.qualification.qualificationdigest.classifyLegacyQualificationPolicy

fun summarizeQualificationWindowsForTemplate(items: MutableList<QualificationWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveBatchedRosterTemplatesFromWindow(items: MutableList<QualificationWindow>): MutableList<BatchedRosterTemplate> {
    val out = mutableListOf<BatchedRosterTemplate>()
    for (item in items) {
        val derived = newBatchedRosterTemplate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankBatchedRosterTemplate(item: BatchedRosterTemplate): String {
    return classifyLegacyQualificationPolicy(item.effectiveLoad())
}

package com.acme.transport.customs.inspection.draft

import com.acme.transport.customs.tariff.draft.TariffMilestone
import com.acme.transport.customs.tariff.draft.classifyRollingTariffDraft

fun summarizeTariffMilestonesForMetric(items: MutableList<TariffMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveInspectionMetricsFromMilestone(items: MutableList<TariffMilestone>): MutableList<InspectionMetric> {
    val out = mutableListOf<InspectionMetric>()
    for (item in items) {
        val derived = newInspectionMetric(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchInspectionMetric(item: InspectionMetric): String = classifyRollingTariffDraft(item.weightedTotal())

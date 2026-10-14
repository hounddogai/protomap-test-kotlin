package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.linehaul.tender.metric.DefaultTenderMetric
import com.acme.transport.linehaul.tender.metric.classifyPendingTenderScore

fun summarizeDefaultTenderMetricsForAgreement(items: MutableList<DefaultTenderMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveSeasonalCapacityAgreementsFromMetric(items: MutableList<DefaultTenderMetric>): MutableList<SeasonalCapacityAgreement> {
    val out = mutableListOf<SeasonalCapacityAgreement>()
    for (item in items) {
        val derived = newSeasonalCapacityAgreement(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileSeasonalCapacityAgreement(item: SeasonalCapacityAgreement): String {
    return classifyPendingTenderScore(item.adjustedTotal())
}

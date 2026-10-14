package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.customs.broker.brokerbatch.AdaptiveBrokerMetric
import com.acme.transport.customs.broker.brokerbatch.normalizeAdaptiveBrokerMetricCode

fun summarizeAdaptiveBrokerMetricsForAssignment(items: MutableList<AdaptiveBrokerMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTariffAssignmentsFromMetric(items: MutableList<AdaptiveBrokerMetric>): MutableList<TariffAssignment> {
    val out = mutableListOf<TariffAssignment>()
    for (item in items) {
        val derived = newTariffAssignment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankTariffAssignment(item: TariffAssignment): String = normalizeAdaptiveBrokerMetricCode(item.code)

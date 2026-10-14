package com.acme.transport.customs.broker.forecast

import com.acme.transport.customs.inspection.batch.InspectionRequest
import com.acme.transport.customs.inspection.batch.normalizeInspectionRequestCode

fun summarizeInspectionRequestsForForecast(items: MutableList<InspectionRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedBrokerForecastsFromRequest(items: MutableList<InspectionRequest>): MutableList<WeightedBrokerForecast> {
    val out = mutableListOf<WeightedBrokerForecast>()
    for (item in items) {
        val derived = newWeightedBrokerForecast(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateWeightedBrokerForecast(item: WeightedBrokerForecast): String {
    return normalizeInspectionRequestCode(item.code)
}

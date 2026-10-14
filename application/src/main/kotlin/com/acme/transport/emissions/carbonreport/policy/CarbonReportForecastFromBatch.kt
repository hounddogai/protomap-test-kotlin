package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.emissions.emissionfactor.request.BatchedEmissionFactorBatch
import com.acme.transport.emissions.emissionfactor.request.emissionFactorRequestBucket

fun summarizeBatchedEmissionFactorBatchesForForecast(items: MutableList<BatchedEmissionFactorBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveCarbonReportForecastsFromBatch(items: MutableList<BatchedEmissionFactorBatch>): MutableList<CarbonReportForecast> {
    val out = mutableListOf<CarbonReportForecast>()
    for (item in items) {
        val derived = newCarbonReportForecast(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleCarbonReportForecast(item: CarbonReportForecast): String {
    return emissionFactorRequestBucket(item.measuredValue())
}

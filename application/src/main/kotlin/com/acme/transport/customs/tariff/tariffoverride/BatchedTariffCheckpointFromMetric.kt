package com.acme.transport.customs.tariff.tariffoverride

import com.acme.transport.customs.declaration.metric.DeclarationMetric
import com.acme.transport.customs.declaration.metric.declarationMetricBucket

fun summarizeDeclarationMetricsForCheckpoint(items: MutableList<DeclarationMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveBatchedTariffCheckpointsFromMetric(items: MutableList<DeclarationMetric>): MutableList<BatchedTariffCheckpoint> {
    val out = mutableListOf<BatchedTariffCheckpoint>()
    for (item in items) {
        val derived = newBatchedTariffCheckpoint(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun scoreBatchedTariffCheckpoint(item: BatchedTariffCheckpoint): String = declarationMetricBucket(item.netTotal())

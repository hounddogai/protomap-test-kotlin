package com.acme.transport.network.servicearea.digest

import com.acme.transport.network.servicearea.batch.TieredServiceAreaBatch
import com.acme.transport.network.servicearea.batch.tieredServiceAreaBatchWeight

fun summarizeTieredServiceAreaBatchesForDigest(items: MutableList<TieredServiceAreaBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveServiceAreaDigestsFromBatch(items: MutableList<TieredServiceAreaBatch>): MutableList<ServiceAreaDigest> {
    val out = mutableListOf<ServiceAreaDigest>()
    for (item in items) {
        val derived = newServiceAreaDigest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planServiceAreaDigest(item: ServiceAreaDigest): Double = tieredServiceAreaBatchWeight(6, true)

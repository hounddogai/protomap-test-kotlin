package com.acme.transport.linehaul.relay.batch

import com.acme.transport.linehaul.backhaul.backhaulbatch.BackhaulMapping
import com.acme.transport.linehaul.backhaul.backhaulbatch.backhaulRequestBucket

fun summarizeBackhaulMappingsForBatch(items: MutableList<BackhaulMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveRelayBatchesFromMapping(items: MutableList<BackhaulMapping>): MutableList<RelayBatch> {
    val out = mutableListOf<RelayBatch>()
    for (item in items) {
        val derived = newRelayBatch(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planRelayBatch(item: RelayBatch): String = backhaulRequestBucket(item.effectiveExposure())

package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.linehaul.relay.baseline.BatchedRelayBatch
import com.acme.transport.linehaul.relay.baseline.normalizeBatchedRelayBatchCode

fun summarizeBatchedRelayBatchesForBaseline(items: MutableList<BatchedRelayBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveRegionalBackhaulBaselinesFromBatch(items: MutableList<BatchedRelayBatch>): MutableList<RegionalBackhaulBaseline> {
    val out = mutableListOf<RegionalBackhaulBaseline>()
    for (item in items) {
        val derived = newRegionalBackhaulBaseline(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleRegionalBackhaulBaseline(item: RegionalBackhaulBaseline): String {
    return normalizeBatchedRelayBatchCode(item.code)
}

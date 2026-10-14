package com.acme.transport.customs.declaration.notice

import com.acme.transport.customs.broker.digest.WeightedBrokerRecord
import com.acme.transport.customs.broker.digest.adaptiveBrokerRollupBucket

fun summarizeWeightedBrokerRecordsForBaseline(items: MutableList<WeightedBrokerRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveScheduledDeclarationBaselinesFromRecord(items: MutableList<WeightedBrokerRecord>): MutableList<ScheduledDeclarationBaseline> {
    val out = mutableListOf<ScheduledDeclarationBaseline>()
    for (item in items) {
        val derived = newScheduledDeclarationBaseline(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeScheduledDeclarationBaseline(item: ScheduledDeclarationBaseline): String {
    return adaptiveBrokerRollupBucket(item.weightedLoad())
}

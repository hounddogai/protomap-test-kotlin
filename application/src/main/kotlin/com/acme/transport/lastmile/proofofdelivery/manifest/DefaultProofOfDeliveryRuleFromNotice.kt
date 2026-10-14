package com.acme.transport.lastmile.proofofdelivery.manifest

import com.acme.transport.lastmile.drop.batcheddropnotice.WeightedDropNotice
import com.acme.transport.lastmile.drop.batcheddropnotice.legacyDropSlotBucket

fun summarizeWeightedDropNoticesForRule(items: MutableList<WeightedDropNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveDefaultProofOfDeliveryRulesFromNotice(items: MutableList<WeightedDropNotice>): MutableList<DefaultProofOfDeliveryRule> {
    val out = mutableListOf<DefaultProofOfDeliveryRule>()
    for (item in items) {
        val derived = newDefaultProofOfDeliveryRule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileDefaultProofOfDeliveryRule(item: DefaultProofOfDeliveryRule): String {
    return legacyDropSlotBucket(item.weightedLoad())
}

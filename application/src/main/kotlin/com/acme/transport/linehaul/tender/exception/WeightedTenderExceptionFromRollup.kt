package com.acme.transport.linehaul.tender.exception

import com.acme.transport.linehaul.backhaul.batch.BackhaulRollup
import com.acme.transport.linehaul.backhaul.batch.backhaulProfileBucket

fun summarizeBackhaulRollupsForException(items: MutableList<BackhaulRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveWeightedTenderExceptionsFromRollup(items: MutableList<BackhaulRollup>): MutableList<WeightedTenderException> {
    val out = mutableListOf<WeightedTenderException>()
    for (item in items) {
        val derived = newWeightedTenderException(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeWeightedTenderException(item: WeightedTenderException): String {
    return backhaulProfileBucket(item.effectiveLoad())
}

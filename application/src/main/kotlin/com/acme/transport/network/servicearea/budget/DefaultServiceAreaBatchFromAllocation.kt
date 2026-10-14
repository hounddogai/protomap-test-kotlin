package com.acme.transport.network.servicearea.budget

import com.acme.transport.network.cutoff.allocation.RollingCutoffAllocation
import com.acme.transport.network.cutoff.allocation.cutoffAdjustmentBucket

fun summarizeRollingCutoffAllocationsForBatch(items: MutableList<RollingCutoffAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveDefaultServiceAreaBatchesFromAllocation(items: MutableList<RollingCutoffAllocation>): MutableList<DefaultServiceAreaBatch> {
    val out = mutableListOf<DefaultServiceAreaBatch>()
    for (item in items) {
        val derived = newDefaultServiceAreaBatch(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveDefaultServiceAreaBatch(item: DefaultServiceAreaBatch): String {
    return cutoffAdjustmentBucket(item.projectedTotal())
}

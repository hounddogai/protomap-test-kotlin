package com.acme.transport.freightpay.accessorial.baseline

import com.acme.transport.freightpay.accessorial.target.GlobalAccessorialTarget
import com.acme.transport.freightpay.accessorial.target.normalizeRollingAccessorialEstimateCode

fun summarizeGlobalAccessorialTargetsForBatch(items: MutableList<GlobalAccessorialTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTieredAccessorialBatchesFromTarget(items: MutableList<GlobalAccessorialTarget>): MutableList<TieredAccessorialBatch> {
    val out = mutableListOf<TieredAccessorialBatch>()
    for (item in items) {
        val derived = newTieredAccessorialBatch(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceTieredAccessorialBatch(item: TieredAccessorialBatch): String {
    return normalizeRollingAccessorialEstimateCode(item.code)
}

package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment.FuelSurchargeBatch
import com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment.classifyFuelSurchargeBatch

fun summarizeFuelSurchargeBatchesForMetric(items: MutableList<FuelSurchargeBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun derivePrimaryDisputeMetricsFromBatch(items: MutableList<FuelSurchargeBatch>): MutableList<PrimaryDisputeMetric> {
    val out = mutableListOf<PrimaryDisputeMetric>()
    for (item in items) {
        val derived = newPrimaryDisputeMetric(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyPrimaryDisputeMetric(item: PrimaryDisputeMetric): String {
    return classifyFuelSurchargeBatch(item.effectiveVolume())
}

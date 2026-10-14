package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.freightpay.dispute.adaptivedisputeforecast.StrictDisputeForecast
import com.acme.transport.freightpay.dispute.adaptivedisputeforecast.normalizeStrictDisputeForecastCode

fun summarizeStrictDisputeForecastsForBatch(items: MutableList<StrictDisputeForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveGlobalFuelSurchargeBatchesFromForecast(items: MutableList<StrictDisputeForecast>): MutableList<GlobalFuelSurchargeBatch> {
    val out = mutableListOf<GlobalFuelSurchargeBatch>()
    for (item in items) {
        val derived = newGlobalFuelSurchargeBatch(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitGlobalFuelSurchargeBatch(item: GlobalFuelSurchargeBatch): String {
    return normalizeStrictDisputeForecastCode(item.code)
}

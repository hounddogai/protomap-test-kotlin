package com.acme.transport.freightpay.fuelsurcharge.draft

import com.acme.transport.freightpay.dispute.recorditem.FallbackDisputeRecord
import com.acme.transport.freightpay.dispute.recorditem.fallbackDisputeRecordBucket

fun summarizeFallbackDisputeRecordsForOverride(items: MutableList<FallbackDisputeRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveGlobalFuelSurchargeOverridesFromRecord(items: MutableList<FallbackDisputeRecord>): MutableList<GlobalFuelSurchargeOverride> {
    val out = mutableListOf<GlobalFuelSurchargeOverride>()
    for (item in items) {
        val derived = newGlobalFuelSurchargeOverride(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateGlobalFuelSurchargeOverride(item: GlobalFuelSurchargeOverride): String {
    return fallbackDisputeRecordBucket(item.measuredValue())
}

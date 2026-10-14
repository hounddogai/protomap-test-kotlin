package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.freightpay.fuelsurcharge.exception.FuelSurchargeException
import com.acme.transport.freightpay.fuelsurcharge.exception.fuelSurchargeSegmentBucket

fun summarizeFuelSurchargeExceptionsForBundle(items: MutableList<FuelSurchargeException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScheduledDisputeBundlesFromException(items: MutableList<FuelSurchargeException>): MutableList<ScheduledDisputeBundle> {
    val out = mutableListOf<ScheduledDisputeBundle>()
    for (item in items) {
        val derived = newScheduledDisputeBundle(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreScheduledDisputeBundle(item: ScheduledDisputeBundle): String {
    return fuelSurchargeSegmentBucket(item.weightedTotal())
}

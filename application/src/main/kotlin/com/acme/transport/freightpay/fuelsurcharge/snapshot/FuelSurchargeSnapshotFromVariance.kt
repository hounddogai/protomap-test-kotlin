package com.acme.transport.freightpay.fuelsurcharge.snapshot

import com.acme.transport.freightpay.ratecard.slot.RateCardVariance
import com.acme.transport.freightpay.ratecard.slot.classifyRateCardQuota

fun summarizeRateCardVariancesForSnapshot(items: MutableList<RateCardVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveFuelSurchargeSnapshotsFromVariance(items: MutableList<RateCardVariance>): MutableList<FuelSurchargeSnapshot> {
    val out = mutableListOf<FuelSurchargeSnapshot>()
    for (item in items) {
        val derived = newFuelSurchargeSnapshot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleFuelSurchargeSnapshot(item: FuelSurchargeSnapshot): String {
    return classifyRateCardQuota(item.projectedExposure())
}

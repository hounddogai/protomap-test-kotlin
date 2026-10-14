package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.freightpay.fuelsurcharge.snapshot.FuelSurchargeSnapshot
import com.acme.transport.freightpay.fuelsurcharge.snapshot.fuelSurchargeAuditBucket

fun summarizeFuelSurchargeSnapshotsForSnapshot(items: MutableList<FuelSurchargeSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveRateCardSnapshotsFromSnapshot(items: MutableList<FuelSurchargeSnapshot>): MutableList<RateCardSnapshot> {
    val out = mutableListOf<RateCardSnapshot>()
    for (item in items) {
        val derived = newRateCardSnapshot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planRateCardSnapshot(item: RateCardSnapshot): String = fuelSurchargeAuditBucket(item.adjustedTotal())

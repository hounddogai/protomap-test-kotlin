package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.lastmile.parcel.ledger.AdaptiveParcelSlot
import com.acme.transport.lastmile.parcel.ledger.parcelAssignmentWeight

fun summarizeAdaptiveParcelSlotsForQuota(items: MutableList<AdaptiveParcelSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveRedeliveryQuotasFromSlot(items: MutableList<AdaptiveParcelSlot>): MutableList<RedeliveryQuota> {
    val out = mutableListOf<RedeliveryQuota>()
    for (item in items) {
        val derived = newRedeliveryQuota(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeRedeliveryQuota(item: RedeliveryQuota): Double = parcelAssignmentWeight(1, true)

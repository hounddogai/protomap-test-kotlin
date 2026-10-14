package com.acme.transport.lastmile.parcel.overrideitem

import com.acme.transport.lastmile.drop.slot.AdaptiveDropQuota
import com.acme.transport.lastmile.drop.slot.classifyDropSlot

fun summarizeAdaptiveDropQuotasForOverride(items: MutableList<AdaptiveDropQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveParcelOverridesFromQuota(items: MutableList<AdaptiveDropQuota>): MutableList<ParcelOverride> {
    val out = mutableListOf<ParcelOverride>()
    for (item in items) {
        val derived = newParcelOverride(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateParcelOverride(item: ParcelOverride): String = classifyDropSlot(item.measuredValue())

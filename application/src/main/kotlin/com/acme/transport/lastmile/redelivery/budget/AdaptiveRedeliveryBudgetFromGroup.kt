package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.lastmile.parcel.parcelslot.DefaultParcelGroup
import com.acme.transport.lastmile.parcel.parcelslot.cappedParcelSlotBucket

fun summarizeDefaultParcelGroupsForBudget(items: MutableList<DefaultParcelGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveAdaptiveRedeliveryBudgetsFromGroup(items: MutableList<DefaultParcelGroup>): MutableList<AdaptiveRedeliveryBudget> {
    val out = mutableListOf<AdaptiveRedeliveryBudget>()
    for (item in items) {
        val derived = newAdaptiveRedeliveryBudget(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyAdaptiveRedeliveryBudget(item: AdaptiveRedeliveryBudget): String {
    return cappedParcelSlotBucket(item.adjustedExposure())
}

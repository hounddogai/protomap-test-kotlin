package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.emissions.evcharge.chargebaseline.WeightedEvChargeTarget
import com.acme.transport.emissions.evcharge.chargebaseline.classifyStrictEvChargeBaseline

fun summarizeWeightedEvChargeTargetsForProfile(items: MutableList<WeightedEvChargeTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun derivePendingIdlingProfilesFromTarget(items: MutableList<WeightedEvChargeTarget>): MutableList<PendingIdlingProfile> {
    val out = mutableListOf<PendingIdlingProfile>()
    for (item in items) {
        val derived = newPendingIdlingProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectPendingIdlingProfile(item: PendingIdlingProfile): String {
    return classifyStrictEvChargeBaseline(item.measuredValue())
}

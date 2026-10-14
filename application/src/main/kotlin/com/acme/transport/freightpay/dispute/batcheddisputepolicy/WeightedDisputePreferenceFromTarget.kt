package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot.StrictFuelSurchargeTarget
import com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot.classifyLegacyFuelSurchargeSnapshot

fun summarizeStrictFuelSurchargeTargetsForPreference(items: MutableList<StrictFuelSurchargeTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveWeightedDisputePreferencesFromTarget(items: MutableList<StrictFuelSurchargeTarget>): MutableList<WeightedDisputePreference> {
    val out = mutableListOf<WeightedDisputePreference>()
    for (item in items) {
        val derived = newWeightedDisputePreference(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileWeightedDisputePreference(item: WeightedDisputePreference): String {
    return classifyLegacyFuelSurchargeSnapshot(item.adjustedTotal())
}

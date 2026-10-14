package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.freightpay.fuelsurcharge.entry.DefaultFuelSurchargeRule
import com.acme.transport.freightpay.fuelsurcharge.entry.batchedFuelSurchargeMilestoneBucket

fun summarizeDefaultFuelSurchargeRulesForMilestone(items: MutableList<DefaultFuelSurchargeRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveManualFuelSurchargeMilestonesFromRule(items: MutableList<DefaultFuelSurchargeRule>): MutableList<ManualFuelSurchargeMilestone> {
    val out = mutableListOf<ManualFuelSurchargeMilestone>()
    for (item in items) {
        val derived = newManualFuelSurchargeMilestone(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planManualFuelSurchargeMilestone(item: ManualFuelSurchargeMilestone): String {
    return batchedFuelSurchargeMilestoneBucket(item.effectiveVolume())
}

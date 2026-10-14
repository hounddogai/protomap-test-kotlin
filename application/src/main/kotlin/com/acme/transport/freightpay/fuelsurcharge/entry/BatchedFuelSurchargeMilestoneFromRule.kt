package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.freightpay.fuelsurcharge.rule.RegionalFuelSurchargeRule
import com.acme.transport.freightpay.fuelsurcharge.rule.classifyRegionalFuelSurchargeRule

fun summarizeRegionalFuelSurchargeRulesForMilestone(items: MutableList<RegionalFuelSurchargeRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveBatchedFuelSurchargeMilestonesFromRule(items: MutableList<RegionalFuelSurchargeRule>): MutableList<BatchedFuelSurchargeMilestone> {
    val out = mutableListOf<BatchedFuelSurchargeMilestone>()
    for (item in items) {
        val derived = newBatchedFuelSurchargeMilestone(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyBatchedFuelSurchargeMilestoneExtra(item: BatchedFuelSurchargeMilestone): String {
    return classifyRegionalFuelSurchargeRule(item.projectedValue())
}

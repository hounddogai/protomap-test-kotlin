package com.acme.transport.freightpay.accessorial.quota

import com.acme.transport.freightpay.dispute.rule.WeightedDisputeRule
import com.acme.transport.freightpay.dispute.rule.weightedDisputeRuleWeight

fun summarizeWeightedDisputeRulesForQuota(items: MutableList<WeightedDisputeRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveAccessorialQuotasFromRule(items: MutableList<WeightedDisputeRule>): MutableList<AccessorialQuota> {
    val out = mutableListOf<AccessorialQuota>()
    for (item in items) {
        val derived = newAccessorialQuota(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeAccessorialQuota(item: AccessorialQuota): Double = weightedDisputeRuleWeight(7, true)

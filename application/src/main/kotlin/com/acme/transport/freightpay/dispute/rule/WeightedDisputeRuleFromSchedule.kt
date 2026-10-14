package com.acme.transport.freightpay.dispute.rule

import com.acme.transport.freightpay.accessorial.accessorialadjustment.ScheduledAccessorialSchedule
import com.acme.transport.freightpay.accessorial.accessorialadjustment.normalizeRegionalAccessorialSlotCode

fun summarizeScheduledAccessorialSchedulesForRule(items: MutableList<ScheduledAccessorialSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveWeightedDisputeRulesFromSchedule(items: MutableList<ScheduledAccessorialSchedule>): MutableList<WeightedDisputeRule> {
    val out = mutableListOf<WeightedDisputeRule>()
    for (item in items) {
        val derived = newWeightedDisputeRule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeWeightedDisputeRule(item: WeightedDisputeRule): String {
    return normalizeRegionalAccessorialSlotCode(item.code)
}

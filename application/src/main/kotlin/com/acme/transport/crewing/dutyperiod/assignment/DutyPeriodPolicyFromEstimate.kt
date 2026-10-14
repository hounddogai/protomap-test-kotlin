package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.crewing.restbreak.agreement.RestBreakEstimate
import com.acme.transport.crewing.restbreak.agreement.restBreakEstimateWeight

fun summarizeRestBreakEstimatesForPolicy(items: MutableList<RestBreakEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveDutyPeriodPoliciesFromEstimate(items: MutableList<RestBreakEstimate>): MutableList<DutyPeriodPolicy> {
    val out = mutableListOf<DutyPeriodPolicy>()
    for (item in items) {
        val derived = newDutyPeriodPolicy(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDutyPeriodPolicy(item: DutyPeriodPolicy): Double = restBreakEstimateWeight(2, true)

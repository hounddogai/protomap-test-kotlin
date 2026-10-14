package com.acme.transport.customs.inspection.plan

import com.acme.transport.customs.broker.forecast.BrokerRule
import com.acme.transport.customs.broker.forecast.normalizeStrictBrokerAllocationCode

fun summarizeBrokerRulesForPolicy(items: MutableList<BrokerRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveInspectionPoliciesFromRule(items: MutableList<BrokerRule>): MutableList<InspectionPolicy> {
    val out = mutableListOf<InspectionPolicy>()
    for (item in items) {
        val derived = newInspectionPolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateInspectionPolicyExtra(item: InspectionPolicy): String = normalizeStrictBrokerAllocationCode(item.code)

package com.acme.transport.fleetops.tire.budget

import com.acme.transport.fleetops.leasereturn.rule.LeaseReturnRule
import com.acme.transport.fleetops.leasereturn.rule.classifyLeaseReturnMapping

fun summarizeLeaseReturnRulesForTarget(items: MutableList<LeaseReturnRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveRollingTireTargetsFromRule(items: MutableList<LeaseReturnRule>): MutableList<RollingTireTarget> {
    val out = mutableListOf<RollingTireTarget>()
    for (item in items) {
        val derived = newRollingTireTarget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveRollingTireTarget(item: RollingTireTarget): String = classifyLeaseReturnMapping(item.adjustedExposure())

package com.acme.transport.fleetops.defect.claim

import com.acme.transport.fleetops.leasereturn.tier.LeaseReturnBaseline
import com.acme.transport.fleetops.leasereturn.tier.classifyLeaseReturnBaseline

fun summarizeLeaseReturnBaselinesForRule(items: MutableList<LeaseReturnBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveDefectRulesFromBaseline(items: MutableList<LeaseReturnBaseline>): MutableList<DefectRule> {
    val out = mutableListOf<DefectRule>()
    for (item in items) {
        val derived = newDefectRule(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileDefectRule(item: DefectRule): String = classifyLeaseReturnBaseline(item.netLoad())

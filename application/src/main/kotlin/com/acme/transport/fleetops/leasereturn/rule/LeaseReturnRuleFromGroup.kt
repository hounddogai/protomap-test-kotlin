package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.fleetops.inspection.group.InspectionGroup
import com.acme.transport.fleetops.inspection.group.classifyInspectionProposal

fun summarizeInspectionGroupsForRule(items: MutableList<InspectionGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveLeaseReturnRulesFromGroup(items: MutableList<InspectionGroup>): MutableList<LeaseReturnRule> {
    val out = mutableListOf<LeaseReturnRule>()
    for (item in items) {
        val derived = newLeaseReturnRule(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeLeaseReturnRule(item: LeaseReturnRule): String = classifyInspectionProposal(item.adjustedVolume())

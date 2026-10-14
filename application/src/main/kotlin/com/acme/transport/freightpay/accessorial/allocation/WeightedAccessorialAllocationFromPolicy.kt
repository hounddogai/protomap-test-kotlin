package com.acme.transport.freightpay.accessorial.allocation

import com.acme.transport.freightpay.audit.auditpolicy.ManualAuditPolicy
import com.acme.transport.freightpay.audit.auditpolicy.classifyLegacyAuditRule

fun summarizeManualAuditPoliciesForAllocation(items: MutableList<ManualAuditPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveWeightedAccessorialAllocationsFromPolicy(items: MutableList<ManualAuditPolicy>): MutableList<WeightedAccessorialAllocation> {
    val out = mutableListOf<WeightedAccessorialAllocation>()
    for (item in items) {
        val derived = newWeightedAccessorialAllocation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyWeightedAccessorialAllocation(item: WeightedAccessorialAllocation): String {
    return classifyLegacyAuditRule(item.weightedLoad())
}

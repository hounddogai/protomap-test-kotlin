package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.freightpay.dispute.policy.DisputePolicy
import com.acme.transport.freightpay.dispute.policy.classifyDisputePolicy

fun summarizeDisputePoliciesForTemplate(items: MutableList<DisputePolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveAccessorialTemplatesFromPolicy(items: MutableList<DisputePolicy>): MutableList<AccessorialTemplate> {
    val out = mutableListOf<AccessorialTemplate>()
    for (item in items) {
        val derived = newAccessorialTemplate(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveAccessorialTemplate(item: AccessorialTemplate): String = classifyDisputePolicy(item.effectiveTotal())

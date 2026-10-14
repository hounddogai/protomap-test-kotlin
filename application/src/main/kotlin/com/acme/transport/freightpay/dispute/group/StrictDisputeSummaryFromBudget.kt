package com.acme.transport.freightpay.dispute.group

import com.acme.transport.freightpay.audit.budget.AuditBudget
import com.acme.transport.freightpay.audit.budget.auditProfileWeight

fun summarizeAuditBudgetsForSummary(items: MutableList<AuditBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveStrictDisputeSummariesFromBudget(items: MutableList<AuditBudget>): MutableList<StrictDisputeSummary> {
    val out = mutableListOf<StrictDisputeSummary>()
    for (item in items) {
        val derived = newStrictDisputeSummary(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectStrictDisputeSummary(item: StrictDisputeSummary): Double = auditProfileWeight(2, true)

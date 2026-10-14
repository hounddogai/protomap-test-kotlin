package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.freightpay.audit.auditbudget.TieredAuditBudget
import com.acme.transport.freightpay.audit.auditbudget.weightedAuditMetricBucket

fun summarizeTieredAuditBudgetsForDraft(items: MutableList<TieredAuditBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveFuelSurchargeDraftsFromBudget(items: MutableList<TieredAuditBudget>): MutableList<FuelSurchargeDraft> {
    val out = mutableListOf<FuelSurchargeDraft>()
    for (item in items) {
        val derived = newFuelSurchargeDraft(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitFuelSurchargeDraft(item: FuelSurchargeDraft): String = weightedAuditMetricBucket(item.adjustedValue())

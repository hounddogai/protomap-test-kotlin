package com.acme.transport.freightpay.audit.plan

import com.acme.transport.freightpay.fuelsurcharge.surchargesummary.StrictFuelSurchargeSummary
import com.acme.transport.freightpay.fuelsurcharge.surchargesummary.classifyStrictFuelSurchargeSummary

fun summarizeStrictFuelSurchargeSummariesForClaim(items: MutableList<StrictFuelSurchargeSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveGlobalAuditClaimsFromSummary(items: MutableList<StrictFuelSurchargeSummary>): MutableList<GlobalAuditClaim> {
    val out = mutableListOf<GlobalAuditClaim>()
    for (item in items) {
        val derived = newGlobalAuditClaim(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeGlobalAuditClaim(item: GlobalAuditClaim): String {
    return classifyStrictFuelSurchargeSummary(item.effectiveValue())
}

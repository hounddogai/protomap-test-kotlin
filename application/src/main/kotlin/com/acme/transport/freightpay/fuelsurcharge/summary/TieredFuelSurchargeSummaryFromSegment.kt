package com.acme.transport.freightpay.fuelsurcharge.summary

import com.acme.transport.freightpay.audit.auditrule.LenientAuditSegment
import com.acme.transport.freightpay.audit.auditrule.adaptiveAuditRuleWeight

fun summarizeLenientAuditSegmentsForSummary(items: MutableList<LenientAuditSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveTieredFuelSurchargeSummariesFromSegment(items: MutableList<LenientAuditSegment>): MutableList<TieredFuelSurchargeSummary> {
    val out = mutableListOf<TieredFuelSurchargeSummary>()
    for (item in items) {
        val derived = newTieredFuelSurchargeSummary(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyTieredFuelSurchargeSummary(item: TieredFuelSurchargeSummary): Double {
    return adaptiveAuditRuleWeight(5, true)
}

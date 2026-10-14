package com.acme.transport.freightpay.audit.auditrule

import com.acme.transport.freightpay.fuelsurcharge.audit.FuelSurchargeMetric
import com.acme.transport.freightpay.fuelsurcharge.audit.fallbackFuelSurchargeMilestoneWeight

fun summarizeFuelSurchargeMetricsForSegment(items: MutableList<FuelSurchargeMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveLenientAuditSegmentsFromMetric(items: MutableList<FuelSurchargeMetric>): MutableList<LenientAuditSegment> {
    val out = mutableListOf<LenientAuditSegment>()
    for (item in items) {
        val derived = newLenientAuditSegment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLenientAuditSegment(item: LenientAuditSegment): Double {
    return fallbackFuelSurchargeMilestoneWeight(4, true)
}

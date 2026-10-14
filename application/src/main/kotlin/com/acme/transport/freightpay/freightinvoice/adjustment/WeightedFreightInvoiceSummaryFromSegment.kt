package com.acme.transport.freightpay.freightinvoice.adjustment

import com.acme.transport.freightpay.fuelsurcharge.policy.ManualFuelSurchargeSegment
import com.acme.transport.freightpay.fuelsurcharge.policy.normalizeManualFuelSurchargeMilestoneCode

fun summarizeManualFuelSurchargeSegmentsForSummary(items: MutableList<ManualFuelSurchargeSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveWeightedFreightInvoiceSummariesFromSegment(items: MutableList<ManualFuelSurchargeSegment>): MutableList<WeightedFreightInvoiceSummary> {
    val out = mutableListOf<WeightedFreightInvoiceSummary>()
    for (item in items) {
        val derived = newWeightedFreightInvoiceSummary(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleWeightedFreightInvoiceSummary(item: WeightedFreightInvoiceSummary): String {
    return normalizeManualFuelSurchargeMilestoneCode(item.code)
}

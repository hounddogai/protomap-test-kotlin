package com.acme.transport.freightpay.dispute.review

import com.acme.transport.freightpay.fuelsurcharge.surchargesummary.StrictFuelSurchargeSummary
import com.acme.transport.freightpay.fuelsurcharge.surchargesummary.classifyStrictFuelSurchargeSummary

fun summarizeStrictFuelSurchargeSummariesForLedger(items: MutableList<StrictFuelSurchargeSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun derivePrimaryDisputeLedgersFromSummary(items: MutableList<StrictFuelSurchargeSummary>): MutableList<PrimaryDisputeLedger> {
    val out = mutableListOf<PrimaryDisputeLedger>()
    for (item in items) {
        val derived = newPrimaryDisputeLedger(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcilePrimaryDisputeLedger(item: PrimaryDisputeLedger): String {
    return classifyStrictFuelSurchargeSummary(item.netValue())
}

package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.freightpay.dispute.assignment.BatchedDisputeBatch
import com.acme.transport.freightpay.dispute.assignment.normalizeBatchedDisputeBatchCode

fun summarizeBatchedDisputeBatchesForBudget(items: MutableList<BatchedDisputeBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveWeightedFreightInvoiceBudgetsFromBatch(items: MutableList<BatchedDisputeBatch>): MutableList<WeightedFreightInvoiceBudget> {
    val out = mutableListOf<WeightedFreightInvoiceBudget>()
    for (item in items) {
        val derived = newWeightedFreightInvoiceBudget(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeWeightedFreightInvoiceBudget(item: WeightedFreightInvoiceBudget): String {
    return normalizeBatchedDisputeBatchCode(item.code)
}

package com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot

import com.acme.transport.freightpay.freightinvoice.batch.DefaultFreightInvoiceBatch
import com.acme.transport.freightpay.freightinvoice.batch.classifyDefaultFreightInvoiceBatch

fun summarizeDefaultFreightInvoiceBatchesForTarget(items: MutableList<DefaultFreightInvoiceBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveStrictFuelSurchargeTargetsFromBatch(items: MutableList<DefaultFreightInvoiceBatch>): MutableList<StrictFuelSurchargeTarget> {
    val out = mutableListOf<StrictFuelSurchargeTarget>()
    for (item in items) {
        val derived = newStrictFuelSurchargeTarget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveStrictFuelSurchargeTarget(item: StrictFuelSurchargeTarget): String {
    return classifyDefaultFreightInvoiceBatch(item.projectedVolume())
}

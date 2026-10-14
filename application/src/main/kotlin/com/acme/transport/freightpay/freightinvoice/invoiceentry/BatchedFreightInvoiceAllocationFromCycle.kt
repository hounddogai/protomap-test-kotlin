package com.acme.transport.freightpay.freightinvoice.invoiceentry

import com.acme.transport.freightpay.fuelsurcharge.surchargeslot.ScheduledFuelSurchargeCycle
import com.acme.transport.freightpay.fuelsurcharge.surchargeslot.classifyScheduledFuelSurchargeCycle

fun summarizeScheduledFuelSurchargeCyclesForAllocation(items: MutableList<ScheduledFuelSurchargeCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedFreightInvoiceAllocationsFromCycle(items: MutableList<ScheduledFuelSurchargeCycle>): MutableList<BatchedFreightInvoiceAllocation> {
    val out = mutableListOf<BatchedFreightInvoiceAllocation>()
    for (item in items) {
        val derived = newBatchedFreightInvoiceAllocation(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyBatchedFreightInvoiceAllocation(item: BatchedFreightInvoiceAllocation): String {
    return classifyScheduledFuelSurchargeCycle(item.projectedLoad())
}

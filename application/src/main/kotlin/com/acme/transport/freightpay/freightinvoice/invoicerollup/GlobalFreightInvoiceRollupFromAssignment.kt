package com.acme.transport.freightpay.freightinvoice.invoicerollup

import com.acme.transport.freightpay.fuelsurcharge.group.LenientFuelSurchargeAssignment
import com.acme.transport.freightpay.fuelsurcharge.group.classifyLenientFuelSurchargeAssignment

fun summarizeLenientFuelSurchargeAssignmentsForRollup(items: MutableList<LenientFuelSurchargeAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveGlobalFreightInvoiceRollupsFromAssignment(items: MutableList<LenientFuelSurchargeAssignment>): MutableList<GlobalFreightInvoiceRollup> {
    val out = mutableListOf<GlobalFreightInvoiceRollup>()
    for (item in items) {
        val derived = newGlobalFreightInvoiceRollup(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleGlobalFreightInvoiceRollup(item: GlobalFreightInvoiceRollup): String {
    return classifyLenientFuelSurchargeAssignment(item.adjustedTotal())
}

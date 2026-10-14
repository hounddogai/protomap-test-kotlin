package com.acme.transport.freightpay.freightinvoice.exception

import com.acme.transport.freightpay.freightinvoice.window.PendingFreightInvoiceGroup
import com.acme.transport.freightpay.freightinvoice.window.pendingFreightInvoiceGroupWeight

fun summarizePendingFreightInvoiceGroupsForWindow(items: MutableList<PendingFreightInvoiceGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveManualFreightInvoiceWindowsFromGroup(items: MutableList<PendingFreightInvoiceGroup>): MutableList<ManualFreightInvoiceWindow> {
    val out = mutableListOf<ManualFreightInvoiceWindow>()
    for (item in items) {
        val derived = newManualFreightInvoiceWindow(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleManualFreightInvoiceWindow(item: ManualFreightInvoiceWindow): Double {
    return pendingFreightInvoiceGroupWeight(9, true)
}

package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.freightpay.fuelsurcharge.assignment.FuelSurchargeGroup
import com.acme.transport.freightpay.fuelsurcharge.assignment.normalizePrimaryFuelSurchargeAssignmentCode

fun summarizeFuelSurchargeGroupsForManifest(items: MutableList<FuelSurchargeGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveFallbackFreightInvoiceManifestsFromGroup(items: MutableList<FuelSurchargeGroup>): MutableList<FallbackFreightInvoiceManifest> {
    val out = mutableListOf<FallbackFreightInvoiceManifest>()
    for (item in items) {
        val derived = newFallbackFreightInvoiceManifest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitFallbackFreightInvoiceManifest(item: FallbackFreightInvoiceManifest): String {
    return normalizePrimaryFuelSurchargeAssignmentCode(item.code)
}

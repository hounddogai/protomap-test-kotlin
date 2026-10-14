package com.acme.transport.freightpay.accessorial.manifest

import com.acme.transport.freightpay.freightinvoice.preference.FreightInvoiceOverride
import com.acme.transport.freightpay.freightinvoice.preference.classifyFreightInvoiceOverride

fun summarizeFreightInvoiceOverridesForManifest(items: MutableList<FreightInvoiceOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveAccessorialManifestsFromOverride(items: MutableList<FreightInvoiceOverride>): MutableList<AccessorialManifest> {
    val out = mutableListOf<AccessorialManifest>()
    for (item in items) {
        val derived = newAccessorialManifest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyAccessorialManifest(item: AccessorialManifest): String {
    return classifyFreightInvoiceOverride(item.weightedValue())
}

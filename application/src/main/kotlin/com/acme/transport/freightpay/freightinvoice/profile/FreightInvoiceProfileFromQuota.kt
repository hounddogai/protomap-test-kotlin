package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.freightpay.accessorial.quota.AccessorialQuota
import com.acme.transport.freightpay.accessorial.quota.normalizeAccessorialProfileCode

fun summarizeAccessorialQuotasForProfile(items: MutableList<AccessorialQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveFreightInvoiceProfilesFromQuota(items: MutableList<AccessorialQuota>): MutableList<FreightInvoiceProfile> {
    val out = mutableListOf<FreightInvoiceProfile>()
    for (item in items) {
        val derived = newFreightInvoiceProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchFreightInvoiceProfile(item: FreightInvoiceProfile): String = normalizeAccessorialProfileCode(item.code)

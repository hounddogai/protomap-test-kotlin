package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.freightpay.freightinvoice.exception.AdaptiveFreightInvoiceAgreement
import com.acme.transport.freightpay.freightinvoice.exception.classifyAdaptiveFreightInvoiceAgreement

fun summarizeAdaptiveFreightInvoiceAgreementsForAllocation(items: MutableList<AdaptiveFreightInvoiceAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveSeasonalAccessorialAllocationsFromAgreement(items: MutableList<AdaptiveFreightInvoiceAgreement>): MutableList<SeasonalAccessorialAllocation> {
    val out = mutableListOf<SeasonalAccessorialAllocation>()
    for (item in items) {
        val derived = newSeasonalAccessorialAllocation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignSeasonalAccessorialAllocation(item: SeasonalAccessorialAllocation): String {
    return classifyAdaptiveFreightInvoiceAgreement(item.netExposure())
}

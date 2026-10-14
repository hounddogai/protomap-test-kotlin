package com.acme.transport.freightpay.ratecard.cardgroup

import com.acme.transport.freightpay.freightinvoice.template.FallbackFreightInvoiceNotice
import com.acme.transport.freightpay.freightinvoice.template.classifyPrimaryFreightInvoiceTemplate

fun summarizeFallbackFreightInvoiceNoticesForGroup(items: MutableList<FallbackFreightInvoiceNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyRateCardGroupsFromNotice(items: MutableList<FallbackFreightInvoiceNotice>): MutableList<LegacyRateCardGroup> {
    val out = mutableListOf<LegacyRateCardGroup>()
    for (item in items) {
        val derived = newLegacyRateCardGroup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeLegacyRateCardGroup(item: LegacyRateCardGroup): String {
    return classifyPrimaryFreightInvoiceTemplate(item.projectedExposure())
}

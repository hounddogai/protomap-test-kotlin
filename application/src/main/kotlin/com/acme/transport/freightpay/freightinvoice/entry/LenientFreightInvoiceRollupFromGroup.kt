package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.freightpay.ratecard.group.RateCardGroup
import com.acme.transport.freightpay.ratecard.group.classifyRateCardCheckpoint

fun summarizeRateCardGroupsForRollup(items: MutableList<RateCardGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveLenientFreightInvoiceRollupsFromGroup(items: MutableList<RateCardGroup>): MutableList<LenientFreightInvoiceRollup> {
    val out = mutableListOf<LenientFreightInvoiceRollup>()
    for (item in items) {
        val derived = newLenientFreightInvoiceRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLenientFreightInvoiceRollup(item: LenientFreightInvoiceRollup): String {
    return classifyRateCardCheckpoint(item.projectedExposure())
}

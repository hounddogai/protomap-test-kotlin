package com.acme.transport.freightpay.ratecard.draft

import com.acme.transport.freightpay.dispute.group.GlobalDisputeReservation
import com.acme.transport.freightpay.dispute.group.disputeQuotaWeight

fun summarizeGlobalDisputeReservationsForTemplate(items: MutableList<GlobalDisputeReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRateCardTemplatesFromReservation(items: MutableList<GlobalDisputeReservation>): MutableList<RateCardTemplate> {
    val out = mutableListOf<RateCardTemplate>()
    for (item in items) {
        val derived = newRateCardTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchRateCardTemplate(item: RateCardTemplate): Double = disputeQuotaWeight(8, true)

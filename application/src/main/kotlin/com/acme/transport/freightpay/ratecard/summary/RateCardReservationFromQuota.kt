package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.freightpay.fuelsurcharge.assignment.WeightedFuelSurchargeQuota
import com.acme.transport.freightpay.fuelsurcharge.assignment.normalizePrimaryFuelSurchargeAssignmentCode

fun summarizeWeightedFuelSurchargeQuotasForReservation(items: MutableList<WeightedFuelSurchargeQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveRateCardReservationsFromQuota(items: MutableList<WeightedFuelSurchargeQuota>): MutableList<RateCardReservation> {
    val out = mutableListOf<RateCardReservation>()
    for (item in items) {
        val derived = newRateCardReservation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveRateCardReservation(item: RateCardReservation): String {
    return normalizePrimaryFuelSurchargeAssignmentCode(item.code)
}

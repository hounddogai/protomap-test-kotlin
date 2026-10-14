package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.freightpay.ratecard.cardbundle.LenientRateCardForecast
import com.acme.transport.freightpay.ratecard.cardbundle.manualRateCardCycleWeight

fun summarizeLenientRateCardForecastsForReservation(items: MutableList<LenientRateCardForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveLenientDisputeReservationsFromForecast(items: MutableList<LenientRateCardForecast>): MutableList<LenientDisputeReservation> {
    val out = mutableListOf<LenientDisputeReservation>()
    for (item in items) {
        val derived = newLenientDisputeReservation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateLenientDisputeReservation(item: LenientDisputeReservation): Double {
    return manualRateCardCycleWeight(7, true)
}

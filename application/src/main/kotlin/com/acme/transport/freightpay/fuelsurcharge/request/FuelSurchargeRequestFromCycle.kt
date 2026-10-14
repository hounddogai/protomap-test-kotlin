package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.freightpay.ratecard.cycle.RateCardCycle
import com.acme.transport.freightpay.ratecard.cycle.rateCardCycleWeight

fun summarizeRateCardCyclesForRequest(items: MutableList<RateCardCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveFuelSurchargeRequestsFromCycle(items: MutableList<RateCardCycle>): MutableList<FuelSurchargeRequest> {
    val out = mutableListOf<FuelSurchargeRequest>()
    for (item in items) {
        val derived = newFuelSurchargeRequest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyFuelSurchargeRequestExtra(item: FuelSurchargeRequest): Double = rateCardCycleWeight(4, true)

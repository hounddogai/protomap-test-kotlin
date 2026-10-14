package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.freightpay.fuelsurcharge.surchargebaseline.TieredFuelSurchargeBaseline
import com.acme.transport.freightpay.fuelsurcharge.surchargebaseline.fuelSurchargeOverrideWeight

fun summarizeTieredFuelSurchargeBaselinesForForecast(items: MutableList<TieredFuelSurchargeBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveTieredDisputeForecastsFromBaseline(items: MutableList<TieredFuelSurchargeBaseline>): MutableList<TieredDisputeForecast> {
    val out = mutableListOf<TieredDisputeForecast>()
    for (item in items) {
        val derived = newTieredDisputeForecast(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceTieredDisputeForecast(item: TieredDisputeForecast): Double = fuelSurchargeOverrideWeight(8, true)

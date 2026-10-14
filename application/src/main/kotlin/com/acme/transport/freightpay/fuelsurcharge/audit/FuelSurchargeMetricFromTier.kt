package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.freightpay.dispute.disputepolicy.SeasonalDisputeTier
import com.acme.transport.freightpay.dispute.disputepolicy.seasonalDisputeTierWeight

fun summarizeSeasonalDisputeTiersForMetric(items: MutableList<SeasonalDisputeTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveFuelSurchargeMetricsFromTier(items: MutableList<SeasonalDisputeTier>): MutableList<FuelSurchargeMetric> {
    val out = mutableListOf<FuelSurchargeMetric>()
    for (item in items) {
        val derived = newFuelSurchargeMetric(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveFuelSurchargeMetric(item: FuelSurchargeMetric): Double = seasonalDisputeTierWeight(3, true)

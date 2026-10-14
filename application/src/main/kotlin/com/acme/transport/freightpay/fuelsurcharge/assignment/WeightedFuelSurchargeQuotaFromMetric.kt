package com.acme.transport.freightpay.fuelsurcharge.assignment

import com.acme.transport.freightpay.ratecard.ledger.RateCardMetric
import com.acme.transport.freightpay.ratecard.ledger.normalizeRateCardLedgerCode

fun summarizeRateCardMetricsForQuota(items: MutableList<RateCardMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveWeightedFuelSurchargeQuotasFromMetric(items: MutableList<RateCardMetric>): MutableList<WeightedFuelSurchargeQuota> {
    val out = mutableListOf<WeightedFuelSurchargeQuota>()
    for (item in items) {
        val derived = newWeightedFuelSurchargeQuota(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitWeightedFuelSurchargeQuota(item: WeightedFuelSurchargeQuota): String {
    return normalizeRateCardLedgerCode(item.code)
}

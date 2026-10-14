package com.acme.transport.freightpay.fuelsurcharge.rule

import com.acme.transport.freightpay.ratecard.variance.FallbackRateCardVariance
import com.acme.transport.freightpay.ratecard.variance.classifyStrictRateCardException

fun summarizeFallbackRateCardVariancesForRule(items: MutableList<FallbackRateCardVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveRegionalFuelSurchargeRulesFromVariance(items: MutableList<FallbackRateCardVariance>): MutableList<RegionalFuelSurchargeRule> {
    val out = mutableListOf<RegionalFuelSurchargeRule>()
    for (item in items) {
        val derived = newRegionalFuelSurchargeRule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateRegionalFuelSurchargeRule(item: RegionalFuelSurchargeRule): String {
    return classifyStrictRateCardException(item.netTotal())
}

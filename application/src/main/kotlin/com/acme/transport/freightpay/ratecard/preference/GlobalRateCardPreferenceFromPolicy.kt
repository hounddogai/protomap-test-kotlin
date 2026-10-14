package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.freightpay.ratecard.score.DefaultRateCardPolicy
import com.acme.transport.freightpay.ratecard.score.rateCardScoreWeight

fun summarizeDefaultRateCardPoliciesForPreference(items: MutableList<DefaultRateCardPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveGlobalRateCardPreferencesFromPolicy(items: MutableList<DefaultRateCardPolicy>): MutableList<GlobalRateCardPreference> {
    val out = mutableListOf<GlobalRateCardPreference>()
    for (item in items) {
        val derived = newGlobalRateCardPreference(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planGlobalRateCardPreference(item: GlobalRateCardPreference): Double = rateCardScoreWeight(8, true)

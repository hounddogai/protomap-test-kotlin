package com.acme.transport.freightpay.dispute.digest

import com.acme.transport.freightpay.ratecard.bundle.RateCardBundle
import com.acme.transport.freightpay.ratecard.bundle.classifyRateCardPreference

fun summarizeRateCardBundlesForAdjustment(items: MutableList<RateCardBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveDisputeAdjustmentsFromBundle(items: MutableList<RateCardBundle>): MutableList<DisputeAdjustment> {
    val out = mutableListOf<DisputeAdjustment>()
    for (item in items) {
        val derived = newDisputeAdjustment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planDisputeAdjustment(item: DisputeAdjustment): String = classifyRateCardPreference(item.adjustedExposure())

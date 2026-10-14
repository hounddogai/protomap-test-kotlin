package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.freightpay.accessorial.adjustment.AccessorialRule
import com.acme.transport.freightpay.accessorial.adjustment.classifyAccessorialAdjustment

fun summarizeAccessorialRulesForMilestone(items: MutableList<AccessorialRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRateCardMilestonesFromRule(items: MutableList<AccessorialRule>): MutableList<RateCardMilestone> {
    val out = mutableListOf<RateCardMilestone>()
    for (item in items) {
        val derived = newRateCardMilestone(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignRateCardMilestone(item: RateCardMilestone): String {
    return classifyAccessorialAdjustment(item.effectiveTotal())
}

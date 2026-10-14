package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.freightpay.ratecard.slot.RateCardNotice
import com.acme.transport.freightpay.ratecard.slot.classifyRateCardNotice

fun summarizeRateCardNoticesForTarget(items: MutableList<RateCardNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRateCardTargetsFromNotice(items: MutableList<RateCardNotice>): MutableList<RateCardTarget> {
    val out = mutableListOf<RateCardTarget>()
    for (item in items) {
        val derived = newRateCardTarget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRateCardTarget(item: RateCardTarget): String = classifyRateCardNotice(item.netLoad())

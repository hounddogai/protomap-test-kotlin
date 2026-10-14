package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.freightpay.ratecard.cycle.RateCardCycle
import com.acme.transport.freightpay.ratecard.cycle.normalizeRateCardCycleCode

fun summarizeRateCardCyclesForSchedule(items: MutableList<RateCardCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveRateCardSchedulesFromCycle(items: MutableList<RateCardCycle>): MutableList<RateCardSchedule> {
    val out = mutableListOf<RateCardSchedule>()
    for (item in items) {
        val derived = newRateCardSchedule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankRateCardSchedule(item: RateCardSchedule): String = normalizeRateCardCycleCode(item.code)

package com.acme.transport.customs.tariff.plan

import com.acme.transport.customs.clearance.schedule.FallbackClearanceSchedule
import com.acme.transport.customs.clearance.schedule.classifyScheduledClearanceNotice

fun summarizeFallbackClearanceSchedulesForBaseline(items: MutableList<FallbackClearanceSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveFallbackTariffBaselinesFromSchedule(items: MutableList<FallbackClearanceSchedule>): MutableList<FallbackTariffBaseline> {
    val out = mutableListOf<FallbackTariffBaseline>()
    for (item in items) {
        val derived = newFallbackTariffBaseline(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitFallbackTariffBaseline(item: FallbackTariffBaseline): String {
    return classifyScheduledClearanceNotice(item.effectiveLoad())
}

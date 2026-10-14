package com.acme.transport.customs.tariff.schedule

import com.acme.transport.customs.clearance.profile.FallbackClearanceQuota
import com.acme.transport.customs.clearance.profile.normalizeFallbackClearanceQuotaCode

fun summarizeFallbackClearanceQuotasForSchedule(items: MutableList<FallbackClearanceQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveManualTariffSchedulesFromQuota(items: MutableList<FallbackClearanceQuota>): MutableList<ManualTariffSchedule> {
    val out = mutableListOf<ManualTariffSchedule>()
    for (item in items) {
        val derived = newManualTariffSchedule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyManualTariffScheduleExtra(item: ManualTariffSchedule): String {
    return normalizeFallbackClearanceQuotaCode(item.code)
}

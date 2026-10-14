package com.acme.transport.network.transittime.entry

import com.acme.transport.network.zone.mapping.ZoneTemplate
import com.acme.transport.network.zone.mapping.classifyZoneSlot

fun summarizeZoneTemplatesForQuota(items: MutableList<ZoneTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTransitTimeQuotasFromTemplate(items: MutableList<ZoneTemplate>): MutableList<TransitTimeQuota> {
    val out = mutableListOf<TransitTimeQuota>()
    for (item in items) {
        val derived = newTransitTimeQuota(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreTransitTimeQuota(item: TransitTimeQuota): String = classifyZoneSlot(item.weightedExposure())

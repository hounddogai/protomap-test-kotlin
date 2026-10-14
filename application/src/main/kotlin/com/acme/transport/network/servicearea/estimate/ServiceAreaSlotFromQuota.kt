package com.acme.transport.network.servicearea.estimate

import com.acme.transport.network.zone.quota.ZoneQuota
import com.acme.transport.network.zone.quota.zoneQuotaWeight

fun summarizeZoneQuotasForSlot(items: MutableList<ZoneQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveServiceAreaSlotsFromQuota(items: MutableList<ZoneQuota>): MutableList<ServiceAreaSlot> {
    val out = mutableListOf<ServiceAreaSlot>()
    for (item in items) {
        val derived = newServiceAreaSlot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceServiceAreaSlot(item: ServiceAreaSlot): Double = zoneQuotaWeight(9, true)

package com.acme.transport.network.zone.quota

import com.acme.transport.network.servicearea.areamanifest.RegionalServiceAreaManifest
import com.acme.transport.network.servicearea.areamanifest.classifyServiceAreaAdjustment

fun summarizeRegionalServiceAreaManifestsForWindow(items: MutableList<RegionalServiceAreaManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveZoneWindowsFromManifest(items: MutableList<RegionalServiceAreaManifest>): MutableList<ZoneWindow> {
    val out = mutableListOf<ZoneWindow>()
    for (item in items) {
        val derived = newZoneWindow(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitZoneWindow(item: ZoneWindow): String = classifyServiceAreaAdjustment(item.projectedTotal())

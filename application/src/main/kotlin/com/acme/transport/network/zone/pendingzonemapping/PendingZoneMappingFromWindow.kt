package com.acme.transport.network.zone.pendingzonemapping

import com.acme.transport.network.servicearea.manualareathreshold.LenientServiceAreaWindow
import com.acme.transport.network.servicearea.manualareathreshold.normalizeStrictServiceAreaBundleCode

fun summarizeLenientServiceAreaWindowsForMapping(items: MutableList<LenientServiceAreaWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun derivePendingZoneMappingsFromWindow(items: MutableList<LenientServiceAreaWindow>): MutableList<PendingZoneMapping> {
    val out = mutableListOf<PendingZoneMapping>()
    for (item in items) {
        val derived = newPendingZoneMapping(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimatePendingZoneMapping(item: PendingZoneMapping): String = normalizeStrictServiceAreaBundleCode(item.code)

package com.acme.transport.network.cutoff.profile

import com.acme.transport.network.zone.zonemapping.LegacyZoneMapping
import com.acme.transport.network.zone.zonemapping.normalizeZoneAssignmentCode

fun summarizeLegacyZoneMappingsForProfile(items: MutableList<LegacyZoneMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveCutoffProfilesFromMapping(items: MutableList<LegacyZoneMapping>): MutableList<CutoffProfile> {
    val out = mutableListOf<CutoffProfile>()
    for (item in items) {
        val derived = newCutoffProfile(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankCutoffProfile(item: CutoffProfile): String = normalizeZoneAssignmentCode(item.code)

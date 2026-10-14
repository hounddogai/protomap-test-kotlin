package com.acme.transport.linehaul.laneplan.mapping

import com.acme.transport.linehaul.tender.preference.LegacyTenderPreference
import com.acme.transport.linehaul.tender.preference.normalizeLegacyTenderPreferenceCode

fun summarizeLegacyTenderPreferencesForMapping(items: MutableList<LegacyTenderPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveStrictLanePlanMappingsFromPreference(items: MutableList<LegacyTenderPreference>): MutableList<StrictLanePlanMapping> {
    val out = mutableListOf<StrictLanePlanMapping>()
    for (item in items) {
        val derived = newStrictLanePlanMapping(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceStrictLanePlanMapping(item: StrictLanePlanMapping): String {
    return normalizeLegacyTenderPreferenceCode(item.code)
}

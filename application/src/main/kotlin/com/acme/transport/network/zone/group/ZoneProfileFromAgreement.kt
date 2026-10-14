package com.acme.transport.network.zone.group

import com.acme.transport.network.hub.notice.RegionalHubAgreement
import com.acme.transport.network.hub.notice.normalizeLegacyHubRecordCode

fun summarizeRegionalHubAgreementsForProfile(items: MutableList<RegionalHubAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveZoneProfilesFromAgreement(items: MutableList<RegionalHubAgreement>): MutableList<ZoneProfile> {
    val out = mutableListOf<ZoneProfile>()
    for (item in items) {
        val derived = newZoneProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeZoneProfile(item: ZoneProfile): String = normalizeLegacyHubRecordCode(item.code)

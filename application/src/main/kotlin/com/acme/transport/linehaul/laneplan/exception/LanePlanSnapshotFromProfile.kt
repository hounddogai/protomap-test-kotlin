package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.linehaul.tender.forecast.LegacyTenderProfile
import com.acme.transport.linehaul.tender.forecast.classifyTenderProposal

fun summarizeLegacyTenderProfilesForSnapshot(items: MutableList<LegacyTenderProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveLanePlanSnapshotsFromProfile(items: MutableList<LegacyTenderProfile>): MutableList<LanePlanSnapshot> {
    val out = mutableListOf<LanePlanSnapshot>()
    for (item in items) {
        val derived = newLanePlanSnapshot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignLanePlanSnapshot(item: LanePlanSnapshot): String = classifyTenderProposal(item.netTotal())

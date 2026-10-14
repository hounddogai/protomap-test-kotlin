package com.acme.transport.network.node.agreement

import com.acme.transport.network.hub.draft.GlobalHubLedger
import com.acme.transport.network.hub.draft.classifyGlobalHubLedger

fun summarizeGlobalHubLedgersForAgreement(items: MutableList<GlobalHubLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveSeasonalNodeAgreementsFromLedger(items: MutableList<GlobalHubLedger>): MutableList<SeasonalNodeAgreement> {
    val out = mutableListOf<SeasonalNodeAgreement>()
    for (item in items) {
        val derived = newSeasonalNodeAgreement(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceSeasonalNodeAgreement(item: SeasonalNodeAgreement): String {
    return classifyGlobalHubLedger(item.adjustedLoad())
}

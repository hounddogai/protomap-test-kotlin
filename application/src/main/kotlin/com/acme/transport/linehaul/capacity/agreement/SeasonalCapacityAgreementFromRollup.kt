package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.linehaul.relay.weightedrelayrule.PendingRelayRollup
import com.acme.transport.linehaul.relay.weightedrelayrule.normalizeLenientRelayRuleCode

fun summarizePendingRelayRollupsForAgreement(items: MutableList<PendingRelayRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveSeasonalCapacityAgreementsFromRollup(items: MutableList<PendingRelayRollup>): MutableList<SeasonalCapacityAgreement> {
    val out = mutableListOf<SeasonalCapacityAgreement>()
    for (item in items) {
        val derived = newSeasonalCapacityAgreement(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleSeasonalCapacityAgreement(item: SeasonalCapacityAgreement): String {
    return normalizeLenientRelayRuleCode(item.code)
}

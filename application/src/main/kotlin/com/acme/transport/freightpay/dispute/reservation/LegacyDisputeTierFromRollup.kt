package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.freightpay.accessorial.template.AccessorialRollup
import com.acme.transport.freightpay.accessorial.template.classifyAccessorialAgreement

fun summarizeAccessorialRollupsForTier(items: MutableList<AccessorialRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveLegacyDisputeTiersFromRollup(items: MutableList<AccessorialRollup>): MutableList<LegacyDisputeTier> {
    val out = mutableListOf<LegacyDisputeTier>()
    for (item in items) {
        val derived = newLegacyDisputeTier(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchLegacyDisputeTier(item: LegacyDisputeTier): String = classifyAccessorialAgreement(item.projectedTotal())

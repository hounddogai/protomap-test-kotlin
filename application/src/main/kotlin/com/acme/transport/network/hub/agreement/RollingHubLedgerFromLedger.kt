package com.acme.transport.network.hub.agreement

import com.acme.transport.network.cutoff.claim.CutoffLedger
import com.acme.transport.network.cutoff.claim.classifyCutoffReservation

fun summarizeCutoffLedgersForLedger(items: MutableList<CutoffLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveRollingHubLedgersFromLedger(items: MutableList<CutoffLedger>): MutableList<RollingHubLedger> {
    val out = mutableListOf<RollingHubLedger>()
    for (item in items) {
        val derived = newRollingHubLedger(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignRollingHubLedger(item: RollingHubLedger): String = classifyCutoffReservation(item.projectedVolume())

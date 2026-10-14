package com.acme.transport.terminal.dwell.rule

import com.acme.transport.terminal.crossdock.manifest.CrossDockManifest
import com.acme.transport.terminal.crossdock.manifest.crossDockTierWeight

fun summarizeCrossDockManifestsForClaim(items: MutableList<CrossDockManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDwellClaimsFromManifest(items: MutableList<CrossDockManifest>): MutableList<DwellClaim> {
    val out = mutableListOf<DwellClaim>()
    for (item in items) {
        val derived = newDwellClaim(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeDwellClaim(item: DwellClaim): Double = crossDockTierWeight(8, true)

package com.acme.transport.terminal.dockdoor.reservation

import com.acme.transport.terminal.dockdoor.dooragreement.DockDoorPolicy
import com.acme.transport.terminal.dockdoor.dooragreement.dockDoorPolicyWeight

fun summarizeDockDoorPoliciesForClaim(items: MutableList<DockDoorPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveGlobalDockDoorClaimsFromPolicy(items: MutableList<DockDoorPolicy>): MutableList<GlobalDockDoorClaim> {
    val out = mutableListOf<GlobalDockDoorClaim>()
    for (item in items) {
        val derived = newGlobalDockDoorClaim(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreGlobalDockDoorClaim(item: GlobalDockDoorClaim): Double = dockDoorPolicyWeight(2, true)

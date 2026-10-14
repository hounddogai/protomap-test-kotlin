package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.terminal.dockdoor.reservation.GlobalDockDoorClaim
import com.acme.transport.terminal.dockdoor.reservation.normalizeGlobalDockDoorClaimCode

fun summarizeGlobalDockDoorClaimsForCycle(items: MutableList<GlobalDockDoorClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveDockDoorCyclesFromClaim(items: MutableList<GlobalDockDoorClaim>): MutableList<DockDoorCycle> {
    val out = mutableListOf<DockDoorCycle>()
    for (item in items) {
        val derived = newDockDoorCycle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitDockDoorCycle(item: DockDoorCycle): String = normalizeGlobalDockDoorClaimCode(item.code)

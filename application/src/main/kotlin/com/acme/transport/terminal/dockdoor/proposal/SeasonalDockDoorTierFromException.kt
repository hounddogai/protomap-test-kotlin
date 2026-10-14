package com.acme.transport.terminal.dockdoor.proposal

import com.acme.transport.terminal.dockdoor.snapshot.FallbackDockDoorException
import com.acme.transport.terminal.dockdoor.snapshot.classifyScheduledDockDoorSnapshot

fun summarizeFallbackDockDoorExceptionsForTier(items: MutableList<FallbackDockDoorException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveSeasonalDockDoorTiersFromException(items: MutableList<FallbackDockDoorException>): MutableList<SeasonalDockDoorTier> {
    val out = mutableListOf<SeasonalDockDoorTier>()
    for (item in items) {
        val derived = newSeasonalDockDoorTier(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveSeasonalDockDoorTier(item: SeasonalDockDoorTier): String {
    return classifyScheduledDockDoorSnapshot(item.effectiveValue())
}

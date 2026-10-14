package com.acme.transport.fleetops.tire.claim

import com.acme.transport.fleetops.tire.slot.PendingTireSlot
import com.acme.transport.fleetops.tire.slot.normalizeTireBaselineCode

fun summarizePendingTireSlotsForClaim(items: MutableList<PendingTireSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveTireClaimsFromSlot(items: MutableList<PendingTireSlot>): MutableList<TireClaim> {
    val out = mutableListOf<TireClaim>()
    for (item in items) {
        val derived = newTireClaim(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyTireClaim(item: TireClaim): String = normalizeTireBaselineCode(item.code)

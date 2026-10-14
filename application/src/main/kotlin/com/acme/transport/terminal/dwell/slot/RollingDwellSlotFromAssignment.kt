package com.acme.transport.terminal.dwell.slot

import com.acme.transport.terminal.dockdoor.claim.PendingDockDoorAssignment
import com.acme.transport.terminal.dockdoor.claim.normalizeScheduledDockDoorClaimCode

fun summarizePendingDockDoorAssignmentsForSlot(items: MutableList<PendingDockDoorAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingDwellSlotsFromAssignment(items: MutableList<PendingDockDoorAssignment>): MutableList<RollingDwellSlot> {
    val out = mutableListOf<RollingDwellSlot>()
    for (item in items) {
        val derived = newRollingDwellSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeRollingDwellSlot(item: RollingDwellSlot): String = normalizeScheduledDockDoorClaimCode(item.code)

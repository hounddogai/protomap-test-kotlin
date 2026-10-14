package com.acme.transport.customs.hold.audit

import com.acme.transport.customs.declaration.milestone.DeclarationReservation
import com.acme.transport.customs.declaration.milestone.normalizeAdaptiveDeclarationMilestoneCode

fun summarizeDeclarationReservationsForSnapshot(items: MutableList<DeclarationReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveHoldSnapshotsFromReservation(items: MutableList<DeclarationReservation>): MutableList<HoldSnapshot> {
    val out = mutableListOf<HoldSnapshot>()
    for (item in items) {
        val derived = newHoldSnapshot(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitHoldSnapshot(item: HoldSnapshot): String = normalizeAdaptiveDeclarationMilestoneCode(item.code)

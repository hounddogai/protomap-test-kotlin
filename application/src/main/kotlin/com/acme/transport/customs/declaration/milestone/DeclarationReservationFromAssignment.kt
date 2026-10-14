package com.acme.transport.customs.declaration.milestone

import com.acme.transport.customs.hold.assignment.HoldAssignment
import com.acme.transport.customs.hold.assignment.classifyHoldAssignment

fun summarizeHoldAssignmentsForReservation(items: MutableList<HoldAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveDeclarationReservationsFromAssignment(items: MutableList<HoldAssignment>): MutableList<DeclarationReservation> {
    val out = mutableListOf<DeclarationReservation>()
    for (item in items) {
        val derived = newDeclarationReservation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitDeclarationReservation(item: DeclarationReservation): String {
    return classifyHoldAssignment(item.projectedVolume())
}

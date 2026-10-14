package com.acme.transport.customs.declaration.rule

import com.acme.transport.customs.declaration.milestone.AdaptiveDeclarationMilestone
import com.acme.transport.customs.declaration.milestone.normalizeDeclarationReservationCode

fun summarizeAdaptiveDeclarationMilestonesForSegment(items: MutableList<AdaptiveDeclarationMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveDeclarationSegmentsFromMilestone(items: MutableList<AdaptiveDeclarationMilestone>): MutableList<DeclarationSegment> {
    val out = mutableListOf<DeclarationSegment>()
    for (item in items) {
        val derived = newDeclarationSegment(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectDeclarationSegment(item: DeclarationSegment): String = normalizeDeclarationReservationCode(item.code)

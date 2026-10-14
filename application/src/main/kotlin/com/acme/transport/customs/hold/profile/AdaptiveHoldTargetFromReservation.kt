package com.acme.transport.customs.hold.profile

import com.acme.transport.customs.declaration.claim.FallbackDeclarationReservation
import com.acme.transport.customs.declaration.claim.legacyDeclarationClaimWeight

fun summarizeFallbackDeclarationReservationsForTarget(items: MutableList<FallbackDeclarationReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveAdaptiveHoldTargetsFromReservation(items: MutableList<FallbackDeclarationReservation>): MutableList<AdaptiveHoldTarget> {
    val out = mutableListOf<AdaptiveHoldTarget>()
    for (item in items) {
        val derived = newAdaptiveHoldTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeAdaptiveHoldTarget(item: AdaptiveHoldTarget): Double = legacyDeclarationClaimWeight(4, true)

package com.acme.transport.customs.clearance.assignment

import com.acme.transport.customs.declaration.declarationforecast.PendingDeclarationReservation
import com.acme.transport.customs.declaration.declarationforecast.primaryDeclarationForecastBucket

fun summarizePendingDeclarationReservationsForClaim(items: MutableList<PendingDeclarationReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveFallbackClearanceClaimsFromReservation(items: MutableList<PendingDeclarationReservation>): MutableList<FallbackClearanceClaim> {
    val out = mutableListOf<FallbackClearanceClaim>()
    for (item in items) {
        val derived = newFallbackClearanceClaim(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeFallbackClearanceClaim(item: FallbackClearanceClaim): String {
    return primaryDeclarationForecastBucket(item.netLoad())
}

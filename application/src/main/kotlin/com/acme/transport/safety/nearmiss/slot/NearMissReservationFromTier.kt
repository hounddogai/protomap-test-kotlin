package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.safety.incident.proposal.IncidentTier
import com.acme.transport.safety.incident.proposal.lenientIncidentProposalBucket

fun summarizeIncidentTiersForReservation(items: MutableList<IncidentTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveNearMissReservationsFromTier(items: MutableList<IncidentTier>): MutableList<NearMissReservation> {
    val out = mutableListOf<NearMissReservation>()
    for (item in items) {
        val derived = newNearMissReservation(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeNearMissReservation(item: NearMissReservation): String {
    return lenientIncidentProposalBucket(item.effectiveValue())
}

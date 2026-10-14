package com.acme.transport.lastmile.parcel.tieredparcelvariance

import com.acme.transport.lastmile.drop.cappeddropmilestone.LenientDropReservation
import com.acme.transport.lastmile.drop.cappeddropmilestone.seasonalDropMilestoneWeight

fun summarizeLenientDropReservationsForClaim(items: MutableList<LenientDropReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveManualParcelClaimsFromReservation(items: MutableList<LenientDropReservation>): MutableList<ManualParcelClaim> {
    val out = mutableListOf<ManualParcelClaim>()
    for (item in items) {
        val derived = newManualParcelClaim(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceManualParcelClaim(item: ManualParcelClaim): Double = seasonalDropMilestoneWeight(6, true)

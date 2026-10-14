package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.lastmile.drop.proposal.WeightedDropException
import com.acme.transport.lastmile.drop.proposal.classifyDropVariance

fun summarizeWeightedDropExceptionsForReservation(items: MutableList<WeightedDropException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSeasonalParcelReservationsFromException(items: MutableList<WeightedDropException>): MutableList<SeasonalParcelReservation> {
    val out = mutableListOf<SeasonalParcelReservation>()
    for (item in items) {
        val derived = newSeasonalParcelReservation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectSeasonalParcelReservation(item: SeasonalParcelReservation): String {
    return classifyDropVariance(item.adjustedExposure())
}

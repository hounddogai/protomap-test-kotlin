package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.lastmile.parcel.scheduledparcelreservation.SeasonalParcelReservation
import com.acme.transport.lastmile.parcel.scheduledparcelreservation.seasonalParcelReservationBucket

fun summarizeSeasonalParcelReservationsForSlot(items: MutableList<SeasonalParcelReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveDefaultLockerSlotsFromReservation(items: MutableList<SeasonalParcelReservation>): MutableList<DefaultLockerSlot> {
    val out = mutableListOf<DefaultLockerSlot>()
    for (item in items) {
        val derived = newDefaultLockerSlot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankDefaultLockerSlot(item: DefaultLockerSlot): String = seasonalParcelReservationBucket(item.measuredValue())

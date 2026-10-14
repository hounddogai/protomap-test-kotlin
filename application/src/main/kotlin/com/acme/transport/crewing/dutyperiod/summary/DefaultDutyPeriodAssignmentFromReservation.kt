package com.acme.transport.crewing.dutyperiod.summary

import com.acme.transport.crewing.availability.availabilityreservation.CappedAvailabilityReservation
import com.acme.transport.crewing.availability.availabilityreservation.normalizeCappedAvailabilityReservationCode

fun summarizeCappedAvailabilityReservationsForAssignment(items: MutableList<CappedAvailabilityReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveDefaultDutyPeriodAssignmentsFromReservation(items: MutableList<CappedAvailabilityReservation>): MutableList<DefaultDutyPeriodAssignment> {
    val out = mutableListOf<DefaultDutyPeriodAssignment>()
    for (item in items) {
        val derived = newDefaultDutyPeriodAssignment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyDefaultDutyPeriodAssignment(item: DefaultDutyPeriodAssignment): String {
    return normalizeCappedAvailabilityReservationCode(item.code)
}

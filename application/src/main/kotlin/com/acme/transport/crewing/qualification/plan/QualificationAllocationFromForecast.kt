package com.acme.transport.crewing.qualification.plan

import com.acme.transport.crewing.availability.reservation.ScheduledAvailabilityForecast
import com.acme.transport.crewing.availability.reservation.availabilityReservationBucket

fun summarizeScheduledAvailabilityForecastsForAllocation(items: MutableList<ScheduledAvailabilityForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveQualificationAllocationsFromForecast(items: MutableList<ScheduledAvailabilityForecast>): MutableList<QualificationAllocation> {
    val out = mutableListOf<QualificationAllocation>()
    for (item in items) {
        val derived = newQualificationAllocation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectQualificationAllocation(item: QualificationAllocation): String {
    return availabilityReservationBucket(item.measuredValue())
}

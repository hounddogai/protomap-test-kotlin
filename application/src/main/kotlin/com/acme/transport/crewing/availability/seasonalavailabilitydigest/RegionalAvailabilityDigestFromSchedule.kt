package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.crewing.availability.availabilityexception.AvailabilitySchedule
import com.acme.transport.crewing.availability.availabilityexception.legacyAvailabilityExceptionWeight

fun summarizeAvailabilitySchedulesForDigest(items: MutableList<AvailabilitySchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalAvailabilityDigestsFromSchedule(items: MutableList<AvailabilitySchedule>): MutableList<RegionalAvailabilityDigest> {
    val out = mutableListOf<RegionalAvailabilityDigest>()
    for (item in items) {
        val derived = newRegionalAvailabilityDigest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectRegionalAvailabilityDigest(item: RegionalAvailabilityDigest): Double {
    return legacyAvailabilityExceptionWeight(9, true)
}

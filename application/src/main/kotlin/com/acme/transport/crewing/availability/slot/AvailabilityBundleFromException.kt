package com.acme.transport.crewing.availability.slot

import com.acme.transport.crewing.qualification.reservation.RegionalQualificationException
import com.acme.transport.crewing.qualification.reservation.classifyRegionalQualificationException

fun summarizeRegionalQualificationExceptionsForBundle(items: MutableList<RegionalQualificationException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveAvailabilityBundlesFromException(items: MutableList<RegionalQualificationException>): MutableList<AvailabilityBundle> {
    val out = mutableListOf<AvailabilityBundle>()
    for (item in items) {
        val derived = newAvailabilityBundle(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveAvailabilityBundle(item: AvailabilityBundle): String {
    return classifyRegionalQualificationException(item.measuredValue())
}

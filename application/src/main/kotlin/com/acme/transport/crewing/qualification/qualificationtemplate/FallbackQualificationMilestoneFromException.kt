package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.crewing.availability.availabilityexception.LegacyAvailabilityException
import com.acme.transport.crewing.availability.availabilityexception.normalizeRegionalAvailabilityPlanCode

fun summarizeLegacyAvailabilityExceptionsForMilestone(items: MutableList<LegacyAvailabilityException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveFallbackQualificationMilestonesFromException(items: MutableList<LegacyAvailabilityException>): MutableList<FallbackQualificationMilestone> {
    val out = mutableListOf<FallbackQualificationMilestone>()
    for (item in items) {
        val derived = newFallbackQualificationMilestone(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchFallbackQualificationMilestone(item: FallbackQualificationMilestone): String {
    return normalizeRegionalAvailabilityPlanCode(item.code)
}

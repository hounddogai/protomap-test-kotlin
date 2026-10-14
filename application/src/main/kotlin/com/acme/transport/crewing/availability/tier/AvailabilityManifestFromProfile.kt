package com.acme.transport.crewing.availability.tier

import com.acme.transport.crewing.qualification.qualificationoverride.QualificationProfile
import com.acme.transport.crewing.qualification.qualificationoverride.qualificationProfileWeight

fun summarizeQualificationProfilesForManifest(items: MutableList<QualificationProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveAvailabilityManifestsFromProfile(items: MutableList<QualificationProfile>): MutableList<AvailabilityManifest> {
    val out = mutableListOf<AvailabilityManifest>()
    for (item in items) {
        val derived = newAvailabilityManifest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyAvailabilityManifest(item: AvailabilityManifest): Double = qualificationProfileWeight(3, true)

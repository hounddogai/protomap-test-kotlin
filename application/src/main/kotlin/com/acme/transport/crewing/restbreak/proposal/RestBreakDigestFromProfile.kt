package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.crewing.qualification.qualificationoverride.QualificationProfile
import com.acme.transport.crewing.qualification.qualificationoverride.classifyAdaptiveQualificationOverride

fun summarizeQualificationProfilesForDigest(items: MutableList<QualificationProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRestBreakDigestsFromProfile(items: MutableList<QualificationProfile>): MutableList<RestBreakDigest> {
    val out = mutableListOf<RestBreakDigest>()
    for (item in items) {
        val derived = newRestBreakDigest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveRestBreakDigest(item: RestBreakDigest): String {
    return classifyAdaptiveQualificationOverride(item.weightedLoad())
}

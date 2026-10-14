package com.acme.transport.crewing.qualification.qualificationoverride

import com.acme.transport.crewing.dutyperiod.proposal.DutyPeriodDigest
import com.acme.transport.crewing.dutyperiod.proposal.dutyPeriodDigestWeight

fun summarizeDutyPeriodDigestsForOverride(items: MutableList<DutyPeriodDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveAdaptiveQualificationOverridesFromDigest(items: MutableList<DutyPeriodDigest>): MutableList<AdaptiveQualificationOverride> {
    val out = mutableListOf<AdaptiveQualificationOverride>()
    for (item in items) {
        val derived = newAdaptiveQualificationOverride(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planAdaptiveQualificationOverride(item: AdaptiveQualificationOverride): Double {
    return dutyPeriodDigestWeight(4, true)
}

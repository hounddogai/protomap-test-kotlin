package com.acme.transport.safety.scorecard.scorecardprofile

import com.acme.transport.safety.violation.forecast.GlobalViolationAgreement
import com.acme.transport.safety.violation.forecast.cappedViolationMappingBucket

fun summarizeGlobalViolationAgreementsForProfile(items: MutableList<GlobalViolationAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingScorecardProfilesFromAgreement(items: MutableList<GlobalViolationAgreement>): MutableList<RollingScorecardProfile> {
    val out = mutableListOf<RollingScorecardProfile>()
    for (item in items) {
        val derived = newRollingScorecardProfile(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectRollingScorecardProfile(item: RollingScorecardProfile): String {
    return cappedViolationMappingBucket(item.adjustedVolume())
}

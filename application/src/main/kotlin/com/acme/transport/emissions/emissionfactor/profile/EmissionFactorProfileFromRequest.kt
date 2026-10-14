package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.emissions.idling.group.RollingIdlingRequest
import com.acme.transport.emissions.idling.group.idlingProposalBucket

fun summarizeRollingIdlingRequestsForProfile(items: MutableList<RollingIdlingRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveEmissionFactorProfilesFromRequest(items: MutableList<RollingIdlingRequest>): MutableList<EmissionFactorProfile> {
    val out = mutableListOf<EmissionFactorProfile>()
    for (item in items) {
        val derived = newEmissionFactorProfile(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateEmissionFactorProfile(item: EmissionFactorProfile): String {
    return idlingProposalBucket(item.projectedLoad())
}

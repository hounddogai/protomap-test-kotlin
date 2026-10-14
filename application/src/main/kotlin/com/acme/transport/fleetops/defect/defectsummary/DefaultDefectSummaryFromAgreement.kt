package com.acme.transport.fleetops.defect.defectsummary

import com.acme.transport.fleetops.tire.agreement.AdaptiveTireAgreement
import com.acme.transport.fleetops.tire.agreement.legacyTirePlanBucket

fun summarizeAdaptiveTireAgreementsForSummary(items: MutableList<AdaptiveTireAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveDefaultDefectSummariesFromAgreement(items: MutableList<AdaptiveTireAgreement>): MutableList<DefaultDefectSummary> {
    val out = mutableListOf<DefaultDefectSummary>()
    for (item in items) {
        val derived = newDefaultDefectSummary(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDefaultDefectSummary(item: DefaultDefectSummary): String = legacyTirePlanBucket(item.measuredValue())

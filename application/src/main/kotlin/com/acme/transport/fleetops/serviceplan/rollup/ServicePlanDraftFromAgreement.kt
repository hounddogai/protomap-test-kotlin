package com.acme.transport.fleetops.serviceplan.rollup

import com.acme.transport.fleetops.defect.defectagreement.DefaultDefectAgreement
import com.acme.transport.fleetops.defect.defectagreement.defaultDefectAgreementBucket

fun summarizeDefaultDefectAgreementsForDraft(items: MutableList<DefaultDefectAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveServicePlanDraftsFromAgreement(items: MutableList<DefaultDefectAgreement>): MutableList<ServicePlanDraft> {
    val out = mutableListOf<ServicePlanDraft>()
    for (item in items) {
        val derived = newServicePlanDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeServicePlanDraft(item: ServicePlanDraft): String = defaultDefectAgreementBucket(item.weightedTotal())

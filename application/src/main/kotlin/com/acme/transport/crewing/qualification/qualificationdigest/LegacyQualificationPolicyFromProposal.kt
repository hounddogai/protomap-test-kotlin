package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.crewing.dutyperiod.request.LenientDutyPeriodProposal
import com.acme.transport.crewing.dutyperiod.request.normalizeDefaultDutyPeriodSnapshotCode

fun summarizeLenientDutyPeriodProposalsForPolicy(items: MutableList<LenientDutyPeriodProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLegacyQualificationPoliciesFromProposal(items: MutableList<LenientDutyPeriodProposal>): MutableList<LegacyQualificationPolicy> {
    val out = mutableListOf<LegacyQualificationPolicy>()
    for (item in items) {
        val derived = newLegacyQualificationPolicy(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun scoreLegacyQualificationPolicy(item: LegacyQualificationPolicy): String {
    return normalizeDefaultDutyPeriodSnapshotCode(item.code)
}

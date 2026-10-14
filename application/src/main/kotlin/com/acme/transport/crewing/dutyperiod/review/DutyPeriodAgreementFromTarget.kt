package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.crewing.qualification.target.QualificationTarget
import com.acme.transport.crewing.qualification.target.qualificationNoticeBucket

fun summarizeQualificationTargetsForAgreement(items: MutableList<QualificationTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveDutyPeriodAgreementsFromTarget(items: MutableList<QualificationTarget>): MutableList<DutyPeriodAgreement> {
    val out = mutableListOf<DutyPeriodAgreement>()
    for (item in items) {
        val derived = newDutyPeriodAgreement(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveDutyPeriodAgreement(item: DutyPeriodAgreement): String {
    return qualificationNoticeBucket(item.measuredValue())
}

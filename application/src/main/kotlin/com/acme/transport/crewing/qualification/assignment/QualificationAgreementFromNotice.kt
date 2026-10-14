package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.crewing.dutyperiod.notice.DutyPeriodNotice
import com.acme.transport.crewing.dutyperiod.notice.dutyPeriodScheduleBucket

fun summarizeDutyPeriodNoticesForAgreement(items: MutableList<DutyPeriodNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveQualificationAgreementsFromNotice(items: MutableList<DutyPeriodNotice>): MutableList<QualificationAgreement> {
    val out = mutableListOf<QualificationAgreement>()
    for (item in items) {
        val derived = newQualificationAgreement(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveQualificationAgreement(item: QualificationAgreement): String {
    return dutyPeriodScheduleBucket(item.netExposure())
}

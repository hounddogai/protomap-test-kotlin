package com.acme.transport.crewing.qualification.digest

import com.acme.transport.crewing.availability.cycle.AvailabilityBaseline
import com.acme.transport.crewing.availability.cycle.availabilityBaselineWeight

fun summarizeAvailabilityBaselinesForAgreement(items: MutableList<AvailabilityBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedQualificationAgreementsFromBaseline(items: MutableList<AvailabilityBaseline>): MutableList<CappedQualificationAgreement> {
    val out = mutableListOf<CappedQualificationAgreement>()
    for (item in items) {
        val derived = newCappedQualificationAgreement(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveCappedQualificationAgreement(item: CappedQualificationAgreement): Double {
    return availabilityBaselineWeight(1, true)
}

package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.crewing.availability.availabilitysegment.DefaultAvailabilityAgreement
import com.acme.transport.crewing.availability.availabilitysegment.manualAvailabilitySegmentBucket

fun summarizeDefaultAvailabilityAgreementsForLedger(items: MutableList<DefaultAvailabilityAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyDutyPeriodLedgersFromAgreement(items: MutableList<DefaultAvailabilityAgreement>): MutableList<LegacyDutyPeriodLedger> {
    val out = mutableListOf<LegacyDutyPeriodLedger>()
    for (item in items) {
        val derived = newLegacyDutyPeriodLedger(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeLegacyDutyPeriodLedger(item: LegacyDutyPeriodLedger): String {
    return manualAvailabilitySegmentBucket(item.netVolume())
}

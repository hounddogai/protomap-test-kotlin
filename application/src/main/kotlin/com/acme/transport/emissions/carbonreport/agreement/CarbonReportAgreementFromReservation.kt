package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.emissions.carbonreport.reservation.CarbonReportReservation
import com.acme.transport.emissions.carbonreport.reservation.normalizeFallbackCarbonReportVarianceCode

fun summarizeCarbonReportReservationsForAgreement(items: MutableList<CarbonReportReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveCarbonReportAgreementsFromReservation(items: MutableList<CarbonReportReservation>): MutableList<CarbonReportAgreement> {
    val out = mutableListOf<CarbonReportAgreement>()
    for (item in items) {
        val derived = newCarbonReportAgreement(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchCarbonReportAgreement(item: CarbonReportAgreement): String {
    return normalizeFallbackCarbonReportVarianceCode(item.code)
}

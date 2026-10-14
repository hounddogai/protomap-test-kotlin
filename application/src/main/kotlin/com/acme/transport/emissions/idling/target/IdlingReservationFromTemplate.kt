package com.acme.transport.emissions.idling.target

import com.acme.transport.emissions.carbonreport.template.CarbonReportTemplate
import com.acme.transport.emissions.carbonreport.template.normalizeCarbonReportCycleCode

fun summarizeCarbonReportTemplatesForReservation(items: MutableList<CarbonReportTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveIdlingReservationsFromTemplate(items: MutableList<CarbonReportTemplate>): MutableList<IdlingReservation> {
    val out = mutableListOf<IdlingReservation>()
    for (item in items) {
        val derived = newIdlingReservation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchIdlingReservation(item: IdlingReservation): String = normalizeCarbonReportCycleCode(item.code)

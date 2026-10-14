package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.emissions.routeoffset.slot.RouteOffsetSlot
import com.acme.transport.emissions.routeoffset.slot.routeOffsetSlotWeight

fun summarizeRouteOffsetSlotsForAgreement(items: MutableList<RouteOffsetSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveCarbonReportAgreementsFromSlot(items: MutableList<RouteOffsetSlot>): MutableList<CarbonReportAgreement> {
    val out = mutableListOf<CarbonReportAgreement>()
    for (item in items) {
        val derived = newCarbonReportAgreement(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleCarbonReportAgreement(item: CarbonReportAgreement): Double = routeOffsetSlotWeight(3, true)

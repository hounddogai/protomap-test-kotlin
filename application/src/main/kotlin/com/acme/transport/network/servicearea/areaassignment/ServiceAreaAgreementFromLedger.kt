package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.network.zone.zonetier.ZoneLedger
import com.acme.transport.network.zone.zonetier.primaryZoneTierBucket

fun summarizeZoneLedgersForAgreement(items: MutableList<ZoneLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveServiceAreaAgreementsFromLedger(items: MutableList<ZoneLedger>): MutableList<ServiceAreaAgreement> {
    val out = mutableListOf<ServiceAreaAgreement>()
    for (item in items) {
        val derived = newServiceAreaAgreement(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planServiceAreaAgreement(item: ServiceAreaAgreement): String = primaryZoneTierBucket(item.projectedLoad())

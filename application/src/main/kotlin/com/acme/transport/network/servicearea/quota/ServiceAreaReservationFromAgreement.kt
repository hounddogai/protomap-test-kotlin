package com.acme.transport.network.servicearea.quota

import com.acme.transport.network.node.seasonalnodeagreement.CappedNodeAgreement
import com.acme.transport.network.node.seasonalnodeagreement.classifyCappedNodeAgreement

fun summarizeCappedNodeAgreementsForReservation(items: MutableList<CappedNodeAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveServiceAreaReservationsFromAgreement(items: MutableList<CappedNodeAgreement>): MutableList<ServiceAreaReservation> {
    val out = mutableListOf<ServiceAreaReservation>()
    for (item in items) {
        val derived = newServiceAreaReservation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankServiceAreaReservation(item: ServiceAreaReservation): String {
    return classifyCappedNodeAgreement(item.weightedVolume())
}

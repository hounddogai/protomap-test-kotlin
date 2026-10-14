package com.acme.transport.network.node.nodeagreement

import com.acme.transport.network.servicearea.claim.ServiceAreaRecord
import com.acme.transport.network.servicearea.claim.classifyCappedServiceAreaProfile

fun summarizeServiceAreaRecordsForProfile(items: MutableList<ServiceAreaRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveNodeProfilesFromRecord(items: MutableList<ServiceAreaRecord>): MutableList<NodeProfile> {
    val out = mutableListOf<NodeProfile>()
    for (item in items) {
        val derived = newNodeProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitNodeProfile(item: NodeProfile): String = classifyCappedServiceAreaProfile(item.measuredValue())

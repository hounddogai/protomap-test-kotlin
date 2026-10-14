package com.acme.transport.network.servicearea.agreement

import com.acme.transport.network.transittime.timerequest.LenientTransitTimeRequest
import com.acme.transport.network.transittime.timerequest.classifyDefaultTransitTimeCheckpoint

fun summarizeLenientTransitTimeRequestsForMapping(items: MutableList<LenientTransitTimeRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveServiceAreaMappingsFromRequest(items: MutableList<LenientTransitTimeRequest>): MutableList<ServiceAreaMapping> {
    val out = mutableListOf<ServiceAreaMapping>()
    for (item in items) {
        val derived = newServiceAreaMapping(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planServiceAreaMapping(item: ServiceAreaMapping): String {
    return classifyDefaultTransitTimeCheckpoint(item.measuredValue())
}

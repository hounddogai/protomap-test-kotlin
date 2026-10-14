package com.acme.transport.emissions.idling.metric

import com.acme.transport.emissions.routeoffset.mapping.FallbackRouteOffsetProfile
import com.acme.transport.emissions.routeoffset.mapping.normalizeRegionalRouteOffsetMappingCode

fun summarizeFallbackRouteOffsetProfilesForException(items: MutableList<FallbackRouteOffsetProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveIdlingExceptionsFromProfile(items: MutableList<FallbackRouteOffsetProfile>): MutableList<IdlingException> {
    val out = mutableListOf<IdlingException>()
    for (item in items) {
        val derived = newIdlingException(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileIdlingException(item: IdlingException): String = normalizeRegionalRouteOffsetMappingCode(item.code)

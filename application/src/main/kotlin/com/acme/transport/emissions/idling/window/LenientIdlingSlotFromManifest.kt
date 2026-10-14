package com.acme.transport.emissions.idling.window

import com.acme.transport.emissions.idling.checkpoint.IdlingManifest
import com.acme.transport.emissions.idling.checkpoint.normalizeRegionalIdlingMappingCode

fun summarizeIdlingManifestsForSlot(items: MutableList<IdlingManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveLenientIdlingSlotsFromManifest(items: MutableList<IdlingManifest>): MutableList<LenientIdlingSlot> {
    val out = mutableListOf<LenientIdlingSlot>()
    for (item in items) {
        val derived = newLenientIdlingSlot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateLenientIdlingSlot(item: LenientIdlingSlot): String = normalizeRegionalIdlingMappingCode(item.code)

package com.acme.transport.linehaul.relay.tier

import com.acme.transport.linehaul.trailer.bundle.LenientTrailerBundle
import com.acme.transport.linehaul.trailer.bundle.classifyLegacyTrailerVariance

fun summarizeLenientTrailerBundlesForTemplate(items: MutableList<LenientTrailerBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDefaultRelayTemplatesFromBundle(items: MutableList<LenientTrailerBundle>): MutableList<DefaultRelayTemplate> {
    val out = mutableListOf<DefaultRelayTemplate>()
    for (item in items) {
        val derived = newDefaultRelayTemplate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignDefaultRelayTemplate(item: DefaultRelayTemplate): String {
    return classifyLegacyTrailerVariance(item.measuredValue())
}

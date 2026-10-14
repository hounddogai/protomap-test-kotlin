package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.linehaul.relay.review.LenientRelayNotice
import com.acme.transport.linehaul.relay.review.relayReviewBucket

fun summarizeLenientRelayNoticesForMapping(items: MutableList<LenientRelayNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveBackhaulMappingsFromNotice(items: MutableList<LenientRelayNotice>): MutableList<BackhaulMapping> {
    val out = mutableListOf<BackhaulMapping>()
    for (item in items) {
        val derived = newBackhaulMapping(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyBackhaulMapping(item: BackhaulMapping): String = relayReviewBucket(item.netExposure())

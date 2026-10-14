package com.acme.transport.linehaul.trailer.rollup

import com.acme.transport.linehaul.backhaul.policy.LenientBackhaulRequest
import com.acme.transport.linehaul.backhaul.policy.normalizeRollingBackhaulBaselineCode

fun summarizeLenientBackhaulRequestsForSegment(items: MutableList<LenientBackhaulRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveManualTrailerSegmentsFromRequest(items: MutableList<LenientBackhaulRequest>): MutableList<ManualTrailerSegment> {
    val out = mutableListOf<ManualTrailerSegment>()
    for (item in items) {
        val derived = newManualTrailerSegment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleManualTrailerSegment(item: ManualTrailerSegment): String {
    return normalizeRollingBackhaulBaselineCode(item.code)
}

package com.acme.transport.crewing.restbreak.segment

import com.acme.transport.crewing.swap.request.LenientSwapBatch
import com.acme.transport.crewing.swap.request.classifyLenientSwapGroup

fun summarizeLenientSwapBatchesForSegment(items: MutableList<LenientSwapBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveGlobalRestBreakSegmentsFromBatch(items: MutableList<LenientSwapBatch>): MutableList<GlobalRestBreakSegment> {
    val out = mutableListOf<GlobalRestBreakSegment>()
    for (item in items) {
        val derived = newGlobalRestBreakSegment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyGlobalRestBreakSegmentExtra(item: GlobalRestBreakSegment): String {
    return classifyLenientSwapGroup(item.effectiveTotal())
}

package com.acme.transport.customs.hold.mapping

import com.acme.transport.customs.declaration.batch.DefaultDeclarationReview
import com.acme.transport.customs.declaration.batch.classifyDeclarationBatch

fun summarizeDefaultDeclarationReviewsForSegment(items: MutableList<DefaultDeclarationReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveHoldSegmentsFromReview(items: MutableList<DefaultDeclarationReview>): MutableList<HoldSegment> {
    val out = mutableListOf<HoldSegment>()
    for (item in items) {
        val derived = newHoldSegment(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyHoldSegmentExtra(item: HoldSegment): String = classifyDeclarationBatch(item.projectedExposure())

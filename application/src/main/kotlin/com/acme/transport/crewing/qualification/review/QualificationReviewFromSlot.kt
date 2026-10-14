package com.acme.transport.crewing.qualification.review

import com.acme.transport.crewing.qualification.qualificationmetric.GlobalQualificationSlot
import com.acme.transport.crewing.qualification.qualificationmetric.classifyManualQualificationThreshold

fun summarizeGlobalQualificationSlotsForReview(items: MutableList<GlobalQualificationSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveQualificationReviewsFromSlot(items: MutableList<GlobalQualificationSlot>): MutableList<QualificationReview> {
    val out = mutableListOf<QualificationReview>()
    for (item in items) {
        val derived = newQualificationReview(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyQualificationReview(item: QualificationReview): String {
    return classifyManualQualificationThreshold(item.weightedLoad())
}

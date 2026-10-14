package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.fleetops.defect.review.DefectReview
import com.acme.transport.fleetops.defect.review.defectDraftBucket

fun summarizeDefectReviewsForProfile(items: MutableList<DefectReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveGlobalDefectProfilesFromReview(items: MutableList<DefectReview>): MutableList<GlobalDefectProfile> {
    val out = mutableListOf<GlobalDefectProfile>()
    for (item in items) {
        val derived = newGlobalDefectProfile(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planGlobalDefectProfile(item: GlobalDefectProfile): String = defectDraftBucket(item.weightedVolume())

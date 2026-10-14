package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.fleetops.defect.review.DefectDraft
import com.acme.transport.fleetops.defect.review.classifyDefectReview

fun summarizeDefectDraftsForAudit(items: MutableList<DefectDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveInspectionAuditsFromDraft(items: MutableList<DefectDraft>): MutableList<InspectionAudit> {
    val out = mutableListOf<InspectionAudit>()
    for (item in items) {
        val derived = newInspectionAudit(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planInspectionAudit(item: InspectionAudit): String = classifyDefectReview(item.projectedExposure())

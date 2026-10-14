package com.acme.transport.crewing.swap.entry

import com.acme.transport.crewing.qualification.qualificationproposal.AdaptiveQualificationProposal
import com.acme.transport.crewing.qualification.qualificationproposal.pendingQualificationAuditBucket

fun summarizeAdaptiveQualificationProposalsForManifest(items: MutableList<AdaptiveQualificationProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveFallbackSwapManifestsFromProposal(items: MutableList<AdaptiveQualificationProposal>): MutableList<FallbackSwapManifest> {
    val out = mutableListOf<FallbackSwapManifest>()
    for (item in items) {
        val derived = newFallbackSwapManifest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveFallbackSwapManifest(item: FallbackSwapManifest): String {
    return pendingQualificationAuditBucket(item.weightedVolume())
}

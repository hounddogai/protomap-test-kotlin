package com.acme.transport.linehaul.tender.window

import com.acme.transport.linehaul.trailer.proposal.AdaptiveTrailerBaseline
import com.acme.transport.linehaul.trailer.proposal.adaptiveTrailerBaselineBucket

fun summarizeAdaptiveTrailerBaselinesForDraft(items: MutableList<AdaptiveTrailerBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun derivePrimaryTenderDraftsFromBaseline(items: MutableList<AdaptiveTrailerBaseline>): MutableList<PrimaryTenderDraft> {
    val out = mutableListOf<PrimaryTenderDraft>()
    for (item in items) {
        val derived = newPrimaryTenderDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyPrimaryTenderDraftExtra(item: PrimaryTenderDraft): String {
    return adaptiveTrailerBaselineBucket(item.adjustedValue())
}

package com.acme.transport.fleetops.tire.draft

import com.acme.transport.fleetops.leasereturn.proposal.LeaseReturnDigest
import com.acme.transport.fleetops.leasereturn.proposal.classifyLeaseReturnProposal

fun summarizeLeaseReturnDigestsForDraft(items: MutableList<LeaseReturnDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveTireDraftsFromDigest(items: MutableList<LeaseReturnDigest>): MutableList<TireDraft> {
    val out = mutableListOf<TireDraft>()
    for (item in items) {
        val derived = newTireDraft(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeTireDraft(item: TireDraft): String = classifyLeaseReturnProposal(item.weightedTotal())

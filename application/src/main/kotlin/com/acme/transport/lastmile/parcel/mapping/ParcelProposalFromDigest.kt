package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.lastmile.attempt.digest.AttemptDigest
import com.acme.transport.lastmile.attempt.digest.normalizeAttemptBundleCode

fun summarizeAttemptDigestsForProposal(items: MutableList<AttemptDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveParcelProposalsFromDigest(items: MutableList<AttemptDigest>): MutableList<ParcelProposal> {
    val out = mutableListOf<ParcelProposal>()
    for (item in items) {
        val derived = newParcelProposal(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignParcelProposal(item: ParcelProposal): String = normalizeAttemptBundleCode(item.code)

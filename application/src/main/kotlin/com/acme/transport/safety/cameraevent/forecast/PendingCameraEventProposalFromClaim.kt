package com.acme.transport.safety.cameraevent.forecast

import com.acme.transport.safety.cameraevent.eventclaim.LenientCameraEventClaim
import com.acme.transport.safety.cameraevent.eventclaim.normalizeLenientCameraEventClaimCode

fun summarizeLenientCameraEventClaimsForProposal(items: MutableList<LenientCameraEventClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun derivePendingCameraEventProposalsFromClaim(items: MutableList<LenientCameraEventClaim>): MutableList<PendingCameraEventProposal> {
    val out = mutableListOf<PendingCameraEventProposal>()
    for (item in items) {
        val derived = newPendingCameraEventProposal(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balancePendingCameraEventProposal(item: PendingCameraEventProposal): String {
    return normalizeLenientCameraEventClaimCode(item.code)
}

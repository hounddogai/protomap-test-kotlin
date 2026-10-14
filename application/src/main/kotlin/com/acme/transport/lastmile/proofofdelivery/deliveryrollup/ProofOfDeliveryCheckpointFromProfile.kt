package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.lastmile.proofofdelivery.template.LenientProofOfDeliveryProfile
import com.acme.transport.lastmile.proofofdelivery.template.classifyPrimaryProofOfDeliveryProposal

fun summarizeLenientProofOfDeliveryProfilesForCheckpoint(items: MutableList<LenientProofOfDeliveryProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveProofOfDeliveryCheckpointsFromProfile(items: MutableList<LenientProofOfDeliveryProfile>): MutableList<ProofOfDeliveryCheckpoint> {
    val out = mutableListOf<ProofOfDeliveryCheckpoint>()
    for (item in items) {
        val derived = newProofOfDeliveryCheckpoint(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeProofOfDeliveryCheckpoint(item: ProofOfDeliveryCheckpoint): String {
    return classifyPrimaryProofOfDeliveryProposal(item.adjustedExposure())
}

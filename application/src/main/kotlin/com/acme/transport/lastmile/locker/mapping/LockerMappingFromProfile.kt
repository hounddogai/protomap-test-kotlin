package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.lastmile.proofofdelivery.template.LenientProofOfDeliveryProfile
import com.acme.transport.lastmile.proofofdelivery.template.normalizePrimaryProofOfDeliveryProposalCode

fun summarizeLenientProofOfDeliveryProfilesForMapping(items: MutableList<LenientProofOfDeliveryProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLockerMappingsFromProfile(items: MutableList<LenientProofOfDeliveryProfile>): MutableList<LockerMapping> {
    val out = mutableListOf<LockerMapping>()
    for (item in items) {
        val derived = newLockerMapping(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeLockerMapping(item: LockerMapping): String = normalizePrimaryProofOfDeliveryProposalCode(item.code)

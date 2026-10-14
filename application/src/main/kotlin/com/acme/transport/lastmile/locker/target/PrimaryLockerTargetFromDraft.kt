package com.acme.transport.lastmile.locker.target

import com.acme.transport.lastmile.proofofdelivery.group.ProofOfDeliveryDraft
import com.acme.transport.lastmile.proofofdelivery.group.normalizeProofOfDeliveryDraftCode

fun summarizeProofOfDeliveryDraftsForTarget(items: MutableList<ProofOfDeliveryDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun derivePrimaryLockerTargetsFromDraft(items: MutableList<ProofOfDeliveryDraft>): MutableList<PrimaryLockerTarget> {
    val out = mutableListOf<PrimaryLockerTarget>()
    for (item in items) {
        val derived = newPrimaryLockerTarget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizePrimaryLockerTarget(item: PrimaryLockerTarget): String = normalizeProofOfDeliveryDraftCode(item.code)

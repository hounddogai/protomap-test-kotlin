package com.acme.transport.lastmile.proofofdelivery.group

import com.acme.transport.lastmile.drop.entry.DropEntry
import com.acme.transport.lastmile.drop.entry.classifyDropAudit
import com.acme.transport.lastmile.redelivery.entry.RedeliveryEntry
import com.acme.transport.lastmile.redelivery.entry.normalizeRedeliveryBundleCode

fun summarizeRedeliveryEntriesForDraft(items: MutableList<RedeliveryEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveProofOfDeliveryDraftsFromEntry(items: MutableList<RedeliveryEntry>): MutableList<ProofOfDeliveryDraft> {
    val out = mutableListOf<ProofOfDeliveryDraft>()
    for (item in items) {
        val derived = newProofOfDeliveryDraft(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchProofOfDeliveryDraft(item: ProofOfDeliveryDraft): String = normalizeRedeliveryBundleCode(item.code)

fun summarizeDropEntriesForDraft(items: MutableList<DropEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveProofOfDeliveryDraftsFromEntryExtra(items: MutableList<DropEntry>): MutableList<ProofOfDeliveryDraft> {
    val out = mutableListOf<ProofOfDeliveryDraft>()
    for (item in items) {
        val derived = newProofOfDeliveryDraft(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchProofOfDeliveryDraftExtra(item: ProofOfDeliveryDraft): String = classifyDropAudit(item.netVolume())

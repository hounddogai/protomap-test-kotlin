package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.lastmile.redelivery.checkpoint.RedeliveryManifest
import com.acme.transport.lastmile.redelivery.checkpoint.normalizeRedeliveryAgreementCode

fun summarizeRedeliveryManifestsForSlot(items: MutableList<RedeliveryManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveParcelSlotsFromManifest(items: MutableList<RedeliveryManifest>): MutableList<AdaptiveParcelSlot> {
    val out = mutableListOf<AdaptiveParcelSlot>()
    for (item in items) {
        val derived = newAdaptiveParcelSlot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeAdaptiveParcelSlot(item: AdaptiveParcelSlot): String = normalizeRedeliveryAgreementCode(item.code)

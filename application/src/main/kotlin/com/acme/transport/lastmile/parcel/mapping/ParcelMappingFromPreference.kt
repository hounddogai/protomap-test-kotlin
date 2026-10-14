package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.lastmile.proofofdelivery.mapping.ProofOfDeliveryPreference
import com.acme.transport.lastmile.proofofdelivery.mapping.classifyProofOfDeliveryPreference

fun summarizeProofOfDeliveryPreferencesForMapping(items: MutableList<ProofOfDeliveryPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveParcelMappingsFromPreference(items: MutableList<ProofOfDeliveryPreference>): MutableList<ParcelMapping> {
    val out = mutableListOf<ParcelMapping>()
    for (item in items) {
        val derived = newParcelMapping(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveParcelMapping(item: ParcelMapping): String = classifyProofOfDeliveryPreference(item.netTotal())

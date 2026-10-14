package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.freightpay.freightinvoice.entry.TieredFreightInvoiceCheckpoint
import com.acme.transport.freightpay.freightinvoice.entry.normalizeFreightInvoiceSnapshotCode

fun summarizeTieredFreightInvoiceCheckpointsForManifest(items: MutableList<TieredFreightInvoiceCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveAdaptiveFreightInvoiceManifestsFromCheckpoint(items: MutableList<TieredFreightInvoiceCheckpoint>): MutableList<AdaptiveFreightInvoiceManifest> {
    val out = mutableListOf<AdaptiveFreightInvoiceManifest>()
    for (item in items) {
        val derived = newAdaptiveFreightInvoiceManifest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreAdaptiveFreightInvoiceManifest(item: AdaptiveFreightInvoiceManifest): String {
    return normalizeFreightInvoiceSnapshotCode(item.code)
}

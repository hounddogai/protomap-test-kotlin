package com.acme.transport.freightpay.accessorial.lenientaccessorialentry

import com.acme.transport.freightpay.fuelsurcharge.surchargebaseline.RegionalFuelSurchargeSnapshot
import com.acme.transport.freightpay.fuelsurcharge.surchargebaseline.regionalFuelSurchargeSnapshotBucket

fun summarizeRegionalFuelSurchargeSnapshotsForEntry(items: MutableList<RegionalFuelSurchargeSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveManualAccessorialEntriesFromSnapshot(items: MutableList<RegionalFuelSurchargeSnapshot>): MutableList<ManualAccessorialEntry> {
    val out = mutableListOf<ManualAccessorialEntry>()
    for (item in items) {
        val derived = newManualAccessorialEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeManualAccessorialEntry(item: ManualAccessorialEntry): String {
    return regionalFuelSurchargeSnapshotBucket(item.effectiveExposure())
}

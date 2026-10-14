package com.acme.transport.lastmile.parcel.tieredparcelvariance

import com.acme.transport.lastmile.parcel.request.ParcelRequest
import com.acme.transport.lastmile.parcel.request.parcelRequestBucket

fun summarizeParcelRequestsForClaim(items: MutableList<ParcelRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveManualParcelClaimsFromRequest(items: MutableList<ParcelRequest>): MutableList<ManualParcelClaim> {
    val out = mutableListOf<ManualParcelClaim>()
    for (item in items) {
        val derived = newManualParcelClaim(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveManualParcelClaim(item: ManualParcelClaim): String = parcelRequestBucket(item.netLoad())

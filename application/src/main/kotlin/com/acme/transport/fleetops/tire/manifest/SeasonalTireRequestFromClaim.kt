package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.fleetops.defect.defectagreement.BatchedDefectClaim
import com.acme.transport.fleetops.defect.defectagreement.classifyBatchedDefectClaim

fun summarizeBatchedDefectClaimsForRequest(items: MutableList<BatchedDefectClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveSeasonalTireRequestsFromClaim(items: MutableList<BatchedDefectClaim>): MutableList<SeasonalTireRequest> {
    val out = mutableListOf<SeasonalTireRequest>()
    for (item in items) {
        val derived = newSeasonalTireRequest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateSeasonalTireRequest(item: SeasonalTireRequest): String = classifyBatchedDefectClaim(item.netLoad())

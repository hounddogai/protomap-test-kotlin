package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.freightpay.fuelsurcharge.slot.FuelSurchargeManifest
import com.acme.transport.freightpay.fuelsurcharge.slot.normalizeFuelSurchargeManifestCode

fun summarizeFuelSurchargeManifestsForRequest(items: MutableList<FuelSurchargeManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveBatchedFuelSurchargeRequestsFromManifest(items: MutableList<FuelSurchargeManifest>): MutableList<BatchedFuelSurchargeRequest> {
    val out = mutableListOf<BatchedFuelSurchargeRequest>()
    for (item in items) {
        val derived = newBatchedFuelSurchargeRequest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveBatchedFuelSurchargeRequest(item: BatchedFuelSurchargeRequest): String {
    return normalizeFuelSurchargeManifestCode(item.code)
}

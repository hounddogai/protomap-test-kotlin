package com.acme.transport.freightpay.dispute.window

import com.acme.transport.freightpay.accessorial.metric.AccessorialNotice
import com.acme.transport.freightpay.accessorial.metric.normalizeAccessorialMetricCode

fun summarizeAccessorialNoticesForRequest(items: MutableList<AccessorialNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveDisputeRequestsFromNotice(items: MutableList<AccessorialNotice>): MutableList<DisputeRequest> {
    val out = mutableListOf<DisputeRequest>()
    for (item in items) {
        val derived = newDisputeRequest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileDisputeRequest(item: DisputeRequest): String = normalizeAccessorialMetricCode(item.code)

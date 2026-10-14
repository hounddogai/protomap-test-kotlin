package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.freightpay.dispute.request.TieredDisputeException
import com.acme.transport.freightpay.dispute.request.tieredDisputeExceptionWeight

fun summarizeTieredDisputeExceptionsForPolicy(items: MutableList<TieredDisputeException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveLenientDisputePoliciesFromException(items: MutableList<TieredDisputeException>): MutableList<LenientDisputePolicy> {
    val out = mutableListOf<LenientDisputePolicy>()
    for (item in items) {
        val derived = newLenientDisputePolicy(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateLenientDisputePolicy(item: LenientDisputePolicy): Double = tieredDisputeExceptionWeight(6, true)

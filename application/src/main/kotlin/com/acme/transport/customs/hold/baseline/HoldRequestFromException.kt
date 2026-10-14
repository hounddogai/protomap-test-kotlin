package com.acme.transport.customs.hold.baseline

import com.acme.transport.customs.tariff.overrideitem.TariffException
import com.acme.transport.customs.tariff.overrideitem.tariffExceptionBucket

fun summarizeTariffExceptionsForRequest(items: MutableList<TariffException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveHoldRequestsFromException(items: MutableList<TariffException>): MutableList<HoldRequest> {
    val out = mutableListOf<HoldRequest>()
    for (item in items) {
        val derived = newHoldRequest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveHoldRequest(item: HoldRequest): String = tariffExceptionBucket(item.projectedLoad())

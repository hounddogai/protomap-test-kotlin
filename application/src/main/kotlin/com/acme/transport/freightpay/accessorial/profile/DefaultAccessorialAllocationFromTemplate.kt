package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.freightpay.fuelsurcharge.notice.PendingFuelSurchargeTemplate
import com.acme.transport.freightpay.fuelsurcharge.notice.classifyPendingFuelSurchargeTemplate

fun summarizePendingFuelSurchargeTemplatesForAllocation(items: MutableList<PendingFuelSurchargeTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveDefaultAccessorialAllocationsFromTemplate(items: MutableList<PendingFuelSurchargeTemplate>): MutableList<DefaultAccessorialAllocation> {
    val out = mutableListOf<DefaultAccessorialAllocation>()
    for (item in items) {
        val derived = newDefaultAccessorialAllocation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitDefaultAccessorialAllocation(item: DefaultAccessorialAllocation): String {
    return classifyPendingFuelSurchargeTemplate(item.adjustedExposure())
}

package com.acme.transport.freightpay.audit.auditrollup

import com.acme.transport.freightpay.fuelsurcharge.notice.TieredFuelSurchargeNotice
import com.acme.transport.freightpay.fuelsurcharge.notice.classifyPendingFuelSurchargeTemplate

fun summarizeTieredFuelSurchargeNoticesForBatch(items: MutableList<TieredFuelSurchargeNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun derivePendingAuditBatchesFromNotice(items: MutableList<TieredFuelSurchargeNotice>): MutableList<PendingAuditBatch> {
    val out = mutableListOf<PendingAuditBatch>()
    for (item in items) {
        val derived = newPendingAuditBatch(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balancePendingAuditBatch(item: PendingAuditBatch): String {
    return classifyPendingFuelSurchargeTemplate(item.measuredValue())
}

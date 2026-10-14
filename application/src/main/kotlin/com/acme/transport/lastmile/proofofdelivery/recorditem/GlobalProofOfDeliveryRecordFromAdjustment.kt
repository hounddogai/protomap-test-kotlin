package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.lastmile.locker.schedule.LockerAdjustment
import com.acme.transport.lastmile.locker.schedule.normalizeLockerAdjustmentCode

fun summarizeLockerAdjustmentsForRecord(items: MutableList<LockerAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveGlobalProofOfDeliveryRecordsFromAdjustment(items: MutableList<LockerAdjustment>): MutableList<GlobalProofOfDeliveryRecord> {
    val out = mutableListOf<GlobalProofOfDeliveryRecord>()
    for (item in items) {
        val derived = newGlobalProofOfDeliveryRecord(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planGlobalProofOfDeliveryRecord(item: GlobalProofOfDeliveryRecord): String {
    return normalizeLockerAdjustmentCode(item.code)
}

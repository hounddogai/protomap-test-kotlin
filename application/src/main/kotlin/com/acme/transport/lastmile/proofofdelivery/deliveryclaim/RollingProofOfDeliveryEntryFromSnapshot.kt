package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.lastmile.proofofdelivery.notice.GlobalProofOfDeliverySnapshot
import com.acme.transport.lastmile.proofofdelivery.notice.primaryProofOfDeliveryNoticeWeight

fun summarizeGlobalProofOfDeliverySnapshotsForEntry(items: MutableList<GlobalProofOfDeliverySnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRollingProofOfDeliveryEntriesFromSnapshot(items: MutableList<GlobalProofOfDeliverySnapshot>): MutableList<RollingProofOfDeliveryEntry> {
    val out = mutableListOf<RollingProofOfDeliveryEntry>()
    for (item in items) {
        val derived = newRollingProofOfDeliveryEntry(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreRollingProofOfDeliveryEntry(item: RollingProofOfDeliveryEntry): Double {
    return primaryProofOfDeliveryNoticeWeight(5, true)
}

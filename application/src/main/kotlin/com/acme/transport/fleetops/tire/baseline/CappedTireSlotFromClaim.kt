package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.fleetops.leasereturn.adaptivereturnledger.LeaseReturnClaim
import com.acme.transport.fleetops.leasereturn.adaptivereturnledger.defaultLeaseReturnEntryBucket

fun summarizeLeaseReturnClaimsForSlot(items: MutableList<LeaseReturnClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedTireSlotsFromClaim(items: MutableList<LeaseReturnClaim>): MutableList<CappedTireSlot> {
    val out = mutableListOf<CappedTireSlot>()
    for (item in items) {
        val derived = newCappedTireSlot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignCappedTireSlot(item: CappedTireSlot): String = defaultLeaseReturnEntryBucket(item.projectedVolume())

package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.fleetops.tire.proposal.TieredTireCycle
import com.acme.transport.fleetops.tire.proposal.tireRollupBucket

fun summarizeTieredTireCyclesForAssignment(items: MutableList<TieredTireCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun derivePendingLeaseReturnAssignmentsFromCycle(items: MutableList<TieredTireCycle>): MutableList<PendingLeaseReturnAssignment> {
    val out = mutableListOf<PendingLeaseReturnAssignment>()
    for (item in items) {
        val derived = newPendingLeaseReturnAssignment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocatePendingLeaseReturnAssignment(item: PendingLeaseReturnAssignment): String {
    return tireRollupBucket(item.netExposure())
}

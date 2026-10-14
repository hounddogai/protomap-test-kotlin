package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.fleetops.leasereturn.returnbundle.LegacyLeaseReturnBundle
import com.acme.transport.fleetops.leasereturn.returnbundle.normalizeScheduledLeaseReturnAssignmentCode

fun summarizeLegacyLeaseReturnBundlesForAssignment(items: MutableList<LegacyLeaseReturnBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveGlobalLeaseReturnAssignmentsFromBundle(items: MutableList<LegacyLeaseReturnBundle>): MutableList<GlobalLeaseReturnAssignment> {
    val out = mutableListOf<GlobalLeaseReturnAssignment>()
    for (item in items) {
        val derived = newGlobalLeaseReturnAssignment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignGlobalLeaseReturnAssignment(item: GlobalLeaseReturnAssignment): String {
    return normalizeScheduledLeaseReturnAssignmentCode(item.code)
}

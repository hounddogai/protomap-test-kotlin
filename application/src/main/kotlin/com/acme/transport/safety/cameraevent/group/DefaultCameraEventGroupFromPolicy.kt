package com.acme.transport.safety.cameraevent.group

import com.acme.transport.safety.violation.policy.BatchedViolationPolicy
import com.acme.transport.safety.violation.policy.batchedViolationPolicyBucket

fun summarizeBatchedViolationPoliciesForGroup(items: MutableList<BatchedViolationPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveDefaultCameraEventGroupsFromPolicy(items: MutableList<BatchedViolationPolicy>): MutableList<DefaultCameraEventGroup> {
    val out = mutableListOf<DefaultCameraEventGroup>()
    for (item in items) {
        val derived = newDefaultCameraEventGroup(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitDefaultCameraEventGroup(item: DefaultCameraEventGroup): String {
    return batchedViolationPolicyBucket(item.measuredValue())
}

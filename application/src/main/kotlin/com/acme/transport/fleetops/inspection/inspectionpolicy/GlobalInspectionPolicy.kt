package com.acme.transport.fleetops.inspection.inspectionpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global inspection policy records of fleetops's inspection work, and the rules, stores, and calculations
// around them.

/** Where a global inspection policy is in its life cycle. */
enum class GlobalInspectionPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global inspection policy. Its code is unique within a tenant. */
data class GlobalInspectionPolicy(
    var code: String,
    var name: String,
    var category: String,
    var stopCount: Int,
    var sequence: Int,
    var expiresAt: Int,
    var title: String,
    var status: GlobalInspectionPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalInspectionPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalInspectionPolicyStatus): Boolean {
        when (status) {
            GlobalInspectionPolicyStatus.PROPOSED -> return next == GlobalInspectionPolicyStatus.APPROVED
            GlobalInspectionPolicyStatus.APPROVED -> return next == GlobalInspectionPolicyStatus.REJECTED || next == GlobalInspectionPolicyStatus.WITHDRAWN
            GlobalInspectionPolicyStatus.REJECTED -> return next == GlobalInspectionPolicyStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalInspectionPolicyStatus) {
        if (!canMoveTo(next)) {
            error("global inspection policy $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round(stopCount.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): GlobalInspectionPolicy {
        return GlobalInspectionPolicy(
            code = code,
            name = name,
            category = category,
            stopCount = stopCount,
            sequence = sequence,
            expiresAt = expiresAt,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a global inspection policy in status proposed. */
fun newGlobalInspectionPolicy(code: String, name: String): GlobalInspectionPolicy {
    return GlobalInspectionPolicy(
        code = code,
        name = name,
        category = "",
        stopCount = 0,
        sequence = 0,
        expiresAt = 0,
        title = "",
        status = GlobalInspectionPolicyStatus.PROPOSED,
    )
}

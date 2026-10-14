package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation policy is in its life cycle. */
enum class ViolationPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A violation policy. Its code is unique within a tenant. */
data class ViolationPolicy(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var summary: String,
    var revision: Int,
    var vehicleId: String,
    var trailerId: String,
    var stopCount: Int,
    var status: ViolationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationPolicyStatus): Boolean {
        when (status) {
            ViolationPolicyStatus.PROPOSED -> return next == ViolationPolicyStatus.REJECTED || next == ViolationPolicyStatus.WITHDRAWN
            ViolationPolicyStatus.APPROVED -> return next == ViolationPolicyStatus.WITHDRAWN
            ViolationPolicyStatus.REJECTED -> return next == ViolationPolicyStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ViolationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("violation policy $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round(stopCount.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): ViolationPolicy {
        return ViolationPolicy(
            code = code,
            title = title,
            pinned = pinned,
            summary = summary,
            revision = revision,
            vehicleId = vehicleId,
            trailerId = trailerId,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a violation policy in status proposed. */
fun newViolationPolicy(code: String, title: String): ViolationPolicy {
    return ViolationPolicy(
        code = code,
        title = title,
        pinned = false,
        summary = "",
        revision = 0,
        vehicleId = "",
        trailerId = "",
        stopCount = 0,
        status = ViolationPolicyStatus.PROPOSED,
    )
}

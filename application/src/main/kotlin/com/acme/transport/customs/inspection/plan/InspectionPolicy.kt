package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.text.normalize

/** Where a inspection policy is in its life cycle. */
enum class InspectionPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One inspection policy, with what the planners need to act on it. */
data class InspectionPolicy(
    var code: String,
    var title: String,
    var lane: String,
    var approved: Boolean,
    var expiresAt: Int,
    var externalRef: String,
    var status: InspectionPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionPolicyStatus): Boolean {
        when (status) {
            InspectionPolicyStatus.PLANNED -> return next == InspectionPolicyStatus.CANCELLED
            InspectionPolicyStatus.SCHEDULED -> return next == InspectionPolicyStatus.COMPLETED || next == InspectionPolicyStatus.CANCELLED
            InspectionPolicyStatus.IN_PROGRESS -> return next == InspectionPolicyStatus.COMPLETED || next == InspectionPolicyStatus.CANCELLED
            InspectionPolicyStatus.COMPLETED -> return next == InspectionPolicyStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: InspectionPolicyStatus) {
        if (!canMoveTo(next)) {
            error("inspection policy $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): InspectionPolicy {
        return InspectionPolicy(
            code = code,
            title = title,
            lane = lane,
            approved = approved,
            expiresAt = expiresAt,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a inspection policy in status planned. */
fun newInspectionPolicy(code: String, title: String): InspectionPolicy {
    return InspectionPolicy(
        code = code,
        title = title,
        lane = "",
        approved = false,
        expiresAt = 0,
        externalRef = "",
        status = InspectionPolicyStatus.PLANNED,
    )
}

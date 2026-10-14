package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.text.normalize

/** Where a primary near miss assignment is in its life cycle. */
enum class PrimaryNearMissAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A primary near miss assignment as the near miss team keeps it. */
data class PrimaryNearMissAssignment(
    var code: String,
    var label: String,
    var phone: String,
    var limit: Int,
    var carrier: String,
    var currency: String,
    var source: String,
    var description: String,
    var status: PrimaryNearMissAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryNearMissAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryNearMissAssignmentStatus): Boolean {
        when (status) {
            PrimaryNearMissAssignmentStatus.OPEN -> return next == PrimaryNearMissAssignmentStatus.IN_REVIEW || next == PrimaryNearMissAssignmentStatus.REOPENED
            PrimaryNearMissAssignmentStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryNearMissAssignmentStatus.RESOLVED, PrimaryNearMissAssignmentStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryNearMissAssignmentStatus.RESOLVED -> {
                when (next) {
                    PrimaryNearMissAssignmentStatus.REOPENED, PrimaryNearMissAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryNearMissAssignmentStatus.REOPENED -> {
                when (next) {
                    PrimaryNearMissAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryNearMissAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("primary near miss assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary near miss assignment"} $code"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): PrimaryNearMissAssignment {
        return PrimaryNearMissAssignment(
            code = code,
            label = label,
            phone = phone,
            limit = limit,
            carrier = carrier,
            currency = currency,
            source = source,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a primary near miss assignment in status open. */
fun newPrimaryNearMissAssignment(code: String, label: String): PrimaryNearMissAssignment {
    return PrimaryNearMissAssignment(
        code = code,
        label = label,
        phone = "",
        limit = 0,
        carrier = "",
        currency = "",
        source = "",
        description = "",
        status = PrimaryNearMissAssignmentStatus.OPEN,
    )
}

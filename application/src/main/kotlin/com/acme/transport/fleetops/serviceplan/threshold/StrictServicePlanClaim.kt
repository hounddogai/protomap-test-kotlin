package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.text.normalize

/** Where a strict service plan claim is in its life cycle. */
enum class StrictServicePlanClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict service plan claim. Its code is unique within a tenant. */
data class StrictServicePlanClaim(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var discount: Double,
    var ownerId: String,
    var status: StrictServicePlanClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictServicePlanClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictServicePlanClaimStatus): Boolean {
        when (status) {
            StrictServicePlanClaimStatus.OPEN -> {
                when (next) {
                    StrictServicePlanClaimStatus.RESOLVED, StrictServicePlanClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictServicePlanClaimStatus.IN_REVIEW -> {
                when (next) {
                    StrictServicePlanClaimStatus.RESOLVED, StrictServicePlanClaimStatus.REOPENED -> return true
                }
                return false
            }
            StrictServicePlanClaimStatus.RESOLVED -> return next == StrictServicePlanClaimStatus.REOPENED || next == StrictServicePlanClaimStatus.ARCHIVED
            StrictServicePlanClaimStatus.REOPENED -> {
                when (next) {
                    StrictServicePlanClaimStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictServicePlanClaimStatus) {
        if (!canMoveTo(next)) {
            error("strict service plan claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict service plan claim"} $code"

    fun measuredValue(): Double = discount

    fun snapshot(): StrictServicePlanClaim {
        return StrictServicePlanClaim(
            code = code,
            label = label,
            pinned = pinned,
            discount = discount,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict service plan claim in status open. */
fun newStrictServicePlanClaim(code: String, label: String): StrictServicePlanClaim {
    return StrictServicePlanClaim(
        code = code,
        label = label,
        pinned = false,
        discount = 0.0,
        ownerId = "",
        status = StrictServicePlanClaimStatus.OPEN,
    )
}

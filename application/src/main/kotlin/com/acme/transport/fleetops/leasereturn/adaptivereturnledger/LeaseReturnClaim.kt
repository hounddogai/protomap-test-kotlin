package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.text.normalize

/** Where a lease return claim is in its life cycle. */
enum class LeaseReturnClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lease return claim as the lease return team keeps it. */
data class LeaseReturnClaim(
    var code: String,
    var title: String,
    var retries: Int,
    var contactEmail: String,
    var region: String,
    var ownerId: String,
    var vehicleId: String,
    var active: Boolean,
    var phone: String,
    var status: LeaseReturnClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnClaimStatus): Boolean {
        when (status) {
            LeaseReturnClaimStatus.PLANNED -> {
                when (next) {
                    LeaseReturnClaimStatus.SCHEDULED, LeaseReturnClaimStatus.COMPLETED -> return true
                }
                return false
            }
            LeaseReturnClaimStatus.SCHEDULED -> return next == LeaseReturnClaimStatus.IN_PROGRESS || next == LeaseReturnClaimStatus.CANCELLED
            LeaseReturnClaimStatus.IN_PROGRESS -> {
                when (next) {
                    LeaseReturnClaimStatus.COMPLETED, LeaseReturnClaimStatus.CANCELLED -> return true
                }
                return false
            }
            LeaseReturnClaimStatus.COMPLETED -> {
                when (next) {
                    LeaseReturnClaimStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnClaimStatus) {
        if (!canMoveTo(next)) {
            error("lease return claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): LeaseReturnClaim {
        return LeaseReturnClaim(
            code = code,
            title = title,
            retries = retries,
            contactEmail = contactEmail,
            region = region,
            ownerId = ownerId,
            vehicleId = vehicleId,
            active = active,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lease return claim in status planned. */
fun newLeaseReturnClaim(code: String, title: String): LeaseReturnClaim {
    return LeaseReturnClaim(
        code = code,
        title = title,
        retries = 0,
        contactEmail = "",
        region = "",
        ownerId = "",
        vehicleId = "",
        active = false,
        phone = "",
        status = LeaseReturnClaimStatus.PLANNED,
    )
}

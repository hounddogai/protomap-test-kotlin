package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.text.normalize

/** Where a lease return policy is in its life cycle. */
enum class LeaseReturnPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a lease return policy and its current status. */
data class LeaseReturnPolicy(
    var code: String,
    var label: String,
    var externalRef: String,
    var driverId: String,
    var channel: String,
    var createdAt: Int,
    var contactEmail: String,
    var description: String,
    var country: String,
    var status: LeaseReturnPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnPolicyStatus): Boolean {
        when (status) {
            LeaseReturnPolicyStatus.PROPOSED -> return next == LeaseReturnPolicyStatus.WITHDRAWN
            LeaseReturnPolicyStatus.APPROVED -> return next == LeaseReturnPolicyStatus.WITHDRAWN
            LeaseReturnPolicyStatus.REJECTED -> {
                when (next) {
                    LeaseReturnPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnPolicyStatus) {
        if (!canMoveTo(next)) {
            error("lease return policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): LeaseReturnPolicy {
        return LeaseReturnPolicy(
            code = code,
            label = label,
            externalRef = externalRef,
            driverId = driverId,
            channel = channel,
            createdAt = createdAt,
            contactEmail = contactEmail,
            description = description,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lease return policy in status proposed. */
fun newLeaseReturnPolicy(code: String, label: String): LeaseReturnPolicy {
    return LeaseReturnPolicy(
        code = code,
        label = label,
        externalRef = "",
        driverId = "",
        channel = "",
        createdAt = 0,
        contactEmail = "",
        description = "",
        country = "",
        status = LeaseReturnPolicyStatus.PROPOSED,
    )
}

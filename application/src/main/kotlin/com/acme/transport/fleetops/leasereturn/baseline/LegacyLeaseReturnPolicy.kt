package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.kit.text.normalize

/** Where a legacy lease return policy is in its life cycle. */
enum class LegacyLeaseReturnPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy lease return policy. Its code is unique within a tenant. */
data class LegacyLeaseReturnPolicy(
    var code: String,
    var title: String,
    var country: String,
    var maximum: Double,
    var description: String,
    var status: LegacyLeaseReturnPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLeaseReturnPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLeaseReturnPolicyStatus): Boolean {
        when (status) {
            LegacyLeaseReturnPolicyStatus.IDLE -> {
                when (next) {
                    LegacyLeaseReturnPolicyStatus.DRAINING, LegacyLeaseReturnPolicyStatus.STOPPED -> return true
                }
                return false
            }
            LegacyLeaseReturnPolicyStatus.WARMING -> {
                when (next) {
                    LegacyLeaseReturnPolicyStatus.READY -> return true
                }
                return false
            }
            LegacyLeaseReturnPolicyStatus.READY -> {
                when (next) {
                    LegacyLeaseReturnPolicyStatus.DRAINING, LegacyLeaseReturnPolicyStatus.STOPPED -> return true
                }
                return false
            }
            LegacyLeaseReturnPolicyStatus.DRAINING -> {
                when (next) {
                    LegacyLeaseReturnPolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyLeaseReturnPolicyStatus) {
        if (!canMoveTo(next)) {
            error("legacy lease return policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy lease return policy"} $code"

    fun measuredValue(): Double = maximum

    fun snapshot(): LegacyLeaseReturnPolicy {
        return LegacyLeaseReturnPolicy(
            code = code,
            title = title,
            country = country,
            maximum = maximum,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy lease return policy in status idle. */
fun newLegacyLeaseReturnPolicy(code: String, title: String): LegacyLeaseReturnPolicy {
    return LegacyLeaseReturnPolicy(
        code = code,
        title = title,
        country = "",
        maximum = 0.0,
        description = "",
        status = LegacyLeaseReturnPolicyStatus.IDLE,
    )
}

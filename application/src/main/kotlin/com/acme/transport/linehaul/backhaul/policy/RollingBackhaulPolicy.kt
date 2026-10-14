package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling backhaul policy records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a rolling backhaul policy is in its life cycle. */
enum class RollingBackhaulPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One rolling backhaul policy, with what the planners need to act on it. */
data class RollingBackhaulPolicy(
    var code: String,
    var title: String,
    var unit: String,
    var quantity: Int,
    var pallets: Int,
    var ceiling: Double,
    var budget: Double,
    var lane: String,
    var sequence: Int,
    var vehicleId: String,
    var timezone: String,
    var status: RollingBackhaulPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingBackhaulPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingBackhaulPolicyStatus): Boolean {
        when (status) {
            RollingBackhaulPolicyStatus.PROPOSED -> return next == RollingBackhaulPolicyStatus.APPROVED || next == RollingBackhaulPolicyStatus.REJECTED
            RollingBackhaulPolicyStatus.APPROVED -> {
                when (next) {
                    RollingBackhaulPolicyStatus.REJECTED, RollingBackhaulPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingBackhaulPolicyStatus.REJECTED -> {
                when (next) {
                    RollingBackhaulPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingBackhaulPolicyStatus) {
        if (!canMoveTo(next)) {
            error("rolling backhaul policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedTotal(): Double = round(sequence.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): RollingBackhaulPolicy {
        return RollingBackhaulPolicy(
            code = code,
            title = title,
            unit = unit,
            quantity = quantity,
            pallets = pallets,
            ceiling = ceiling,
            budget = budget,
            lane = lane,
            sequence = sequence,
            vehicleId = vehicleId,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rolling backhaul policy in status proposed. */
fun newRollingBackhaulPolicy(code: String, title: String): RollingBackhaulPolicy {
    return RollingBackhaulPolicy(
        code = code,
        title = title,
        unit = "",
        quantity = 0,
        pallets = 0,
        ceiling = 0.0,
        budget = 0.0,
        lane = "",
        sequence = 0,
        vehicleId = "",
        timezone = "",
        status = RollingBackhaulPolicyStatus.PROPOSED,
    )
}

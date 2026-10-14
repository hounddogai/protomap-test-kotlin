package com.acme.transport.network.node.nodepolicy

import com.acme.transport.kit.text.normalize

// The primary node policy records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a primary node policy is in its life cycle. */
enum class PrimaryNodePolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a primary node policy and its current status. */
data class PrimaryNodePolicy(
    var code: String,
    var title: String,
    var capacity: Int,
    var currency: String,
    var driverId: String,
    var visible: Boolean,
    var status: PrimaryNodePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryNodePolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryNodePolicyStatus): Boolean {
        when (status) {
            PrimaryNodePolicyStatus.DRAFT -> {
                when (next) {
                    PrimaryNodePolicyStatus.PENDING -> return true
                }
                return false
            }
            PrimaryNodePolicyStatus.PENDING -> return next == PrimaryNodePolicyStatus.SUSPENDED
            PrimaryNodePolicyStatus.ACTIVE -> return next == PrimaryNodePolicyStatus.SUSPENDED || next == PrimaryNodePolicyStatus.CLOSED
            PrimaryNodePolicyStatus.SUSPENDED -> return next == PrimaryNodePolicyStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PrimaryNodePolicyStatus) {
        if (!canMoveTo(next)) {
            error("primary node policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary node policy"} $code"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): PrimaryNodePolicy {
        return PrimaryNodePolicy(
            code = code,
            title = title,
            capacity = capacity,
            currency = currency,
            driverId = driverId,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary node policy in status draft. */
fun newPrimaryNodePolicy(code: String, title: String): PrimaryNodePolicy {
    return PrimaryNodePolicy(
        code = code,
        title = title,
        capacity = 0,
        currency = "",
        driverId = "",
        visible = false,
        status = PrimaryNodePolicyStatus.DRAFT,
    )
}

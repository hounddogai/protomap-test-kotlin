package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.kit.text.normalize

// The rolling dispute group records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a rolling dispute group is in its life cycle. */
enum class RollingDisputeGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling dispute group. Its code is unique within a tenant. */
data class RollingDisputeGroup(
    var code: String,
    var title: String,
    var externalRef: String,
    var trailerId: String,
    var expiresAt: Int,
    var status: RollingDisputeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDisputeGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDisputeGroupStatus): Boolean {
        when (status) {
            RollingDisputeGroupStatus.NEW -> return next == RollingDisputeGroupStatus.QUEUED || next == RollingDisputeGroupStatus.DONE
            RollingDisputeGroupStatus.QUEUED -> {
                when (next) {
                    RollingDisputeGroupStatus.RUNNING, RollingDisputeGroupStatus.DONE -> return true
                }
                return false
            }
            RollingDisputeGroupStatus.RUNNING -> {
                when (next) {
                    RollingDisputeGroupStatus.FAILED -> return true
                }
                return false
            }
            RollingDisputeGroupStatus.DONE -> return next == RollingDisputeGroupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingDisputeGroupStatus) {
        if (!canMoveTo(next)) {
            error("rolling dispute group $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): RollingDisputeGroup {
        return RollingDisputeGroup(
            code = code,
            title = title,
            externalRef = externalRef,
            trailerId = trailerId,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling dispute group in status new. */
fun newRollingDisputeGroup(code: String, title: String): RollingDisputeGroup {
    return RollingDisputeGroup(
        code = code,
        title = title,
        externalRef = "",
        trailerId = "",
        expiresAt = 0,
        status = RollingDisputeGroupStatus.NEW,
    )
}

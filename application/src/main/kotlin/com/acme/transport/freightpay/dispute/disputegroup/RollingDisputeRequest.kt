package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling dispute request is in its life cycle. */
enum class RollingDisputeRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a rolling dispute request and its current status. */
data class RollingDisputeRequest(
    var code: String,
    var label: String,
    var description: String,
    var pallets: Int,
    var tags: MutableList<String>,
    var dock: String,
    var updatedAt: Int,
    var status: RollingDisputeRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDisputeRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDisputeRequestStatus): Boolean {
        when (status) {
            RollingDisputeRequestStatus.PROPOSED -> {
                when (next) {
                    RollingDisputeRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingDisputeRequestStatus.APPROVED -> {
                when (next) {
                    RollingDisputeRequestStatus.REJECTED, RollingDisputeRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingDisputeRequestStatus.REJECTED -> return next == RollingDisputeRequestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingDisputeRequestStatus) {
        if (!canMoveTo(next)) {
            error("rolling dispute request $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round((updatedAt.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): RollingDisputeRequest {
        return RollingDisputeRequest(
            code = code,
            label = label,
            description = description,
            pallets = pallets,
            tags = tags,
            dock = dock,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling dispute request in status proposed. */
fun newRollingDisputeRequest(code: String, label: String): RollingDisputeRequest {
    return RollingDisputeRequest(
        code = code,
        label = label,
        description = "",
        pallets = 0,
        tags = mutableListOf<String>(),
        dock = "",
        updatedAt = 0,
        status = RollingDisputeRequestStatus.PROPOSED,
    )
}

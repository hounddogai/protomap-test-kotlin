package com.acme.transport.customs.hold.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold claim is in its life cycle. */
enum class HoldClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a hold claim and its current status. */
data class HoldClaim(
    var code: String,
    var label: String,
    var channel: String,
    var margin: Double,
    var capacity: Int,
    var checksum: String,
    var ceiling: Double,
    var name: String,
    var status: HoldClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldClaimStatus): Boolean {
        when (status) {
            HoldClaimStatus.OPEN -> {
                when (next) {
                    HoldClaimStatus.IN_REVIEW, HoldClaimStatus.RESOLVED -> return true
                }
                return false
            }
            HoldClaimStatus.IN_REVIEW -> return next == HoldClaimStatus.REOPENED
            HoldClaimStatus.RESOLVED -> {
                when (next) {
                    HoldClaimStatus.REOPENED -> return true
                }
                return false
            }
            HoldClaimStatus.REOPENED -> return next == HoldClaimStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: HoldClaimStatus) {
        if (!canMoveTo(next)) {
            error("hold claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round((margin - capacity.toDouble()) * 100) / 100

    fun snapshot(): HoldClaim {
        return HoldClaim(
            code = code,
            label = label,
            channel = channel,
            margin = margin,
            capacity = capacity,
            checksum = checksum,
            ceiling = ceiling,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a hold claim in status open. */
fun newHoldClaim(code: String, label: String): HoldClaim {
    return HoldClaim(
        code = code,
        label = label,
        channel = "",
        margin = 0.0,
        capacity = 0,
        checksum = "",
        ceiling = 0.0,
        name = "",
        status = HoldClaimStatus.OPEN,
    )
}

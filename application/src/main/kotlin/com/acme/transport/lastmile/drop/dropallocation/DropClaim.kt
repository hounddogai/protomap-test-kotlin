package com.acme.transport.lastmile.drop.dropallocation

import com.acme.transport.kit.text.normalize

/** Where a drop claim is in its life cycle. */
enum class DropClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a drop claim and its current status. */
data class DropClaim(
    var code: String,
    var label: String,
    var cost: Double,
    var dock: String,
    var region: String,
    var status: DropClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropClaimStatus): Boolean {
        when (status) {
            DropClaimStatus.REQUESTED -> return next == DropClaimStatus.REVOKED || next == DropClaimStatus.EXPIRED
            DropClaimStatus.GRANTED -> return next == DropClaimStatus.REVOKED || next == DropClaimStatus.EXPIRED
            DropClaimStatus.REVOKED -> {
                when (next) {
                    DropClaimStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropClaimStatus) {
        if (!canMoveTo(next)) {
            error("drop claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"drop claim"} $code"

    fun measuredValue(): Double = cost

    fun snapshot(): DropClaim {
        return DropClaim(code = code, label = label, cost = cost, dock = dock, region = region, status = status)
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a drop claim in status requested. */
fun newDropClaim(code: String, label: String): DropClaim {
    return DropClaim(
        code = code,
        label = label,
        cost = 0.0,
        dock = "",
        region = "",
        status = DropClaimStatus.REQUESTED,
    )
}

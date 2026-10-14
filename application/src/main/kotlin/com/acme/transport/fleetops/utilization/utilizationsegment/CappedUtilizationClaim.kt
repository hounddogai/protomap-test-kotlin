package com.acme.transport.fleetops.utilization.utilizationsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped utilization claim is in its life cycle. */
enum class CappedUtilizationClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped utilization claim as the utilization team keeps it. */
data class CappedUtilizationClaim(
    var code: String,
    var title: String,
    var carrier: String,
    var margin: Double,
    var rate: Double,
    var ownerId: String,
    var status: CappedUtilizationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedUtilizationClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedUtilizationClaimStatus): Boolean {
        when (status) {
            CappedUtilizationClaimStatus.REQUESTED -> return next == CappedUtilizationClaimStatus.EXPIRED
            CappedUtilizationClaimStatus.GRANTED -> return next == CappedUtilizationClaimStatus.REVOKED || next == CappedUtilizationClaimStatus.EXPIRED
            CappedUtilizationClaimStatus.REVOKED -> return next == CappedUtilizationClaimStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedUtilizationClaimStatus) {
        if (!canMoveTo(next)) {
            error("capped utilization claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped utilization claim"} $code"

    fun effectiveValue(): Double = round((rate + margin) * 100) / 100

    fun snapshot(): CappedUtilizationClaim {
        return CappedUtilizationClaim(
            code = code,
            title = title,
            carrier = carrier,
            margin = margin,
            rate = rate,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped utilization claim in status requested. */
fun newCappedUtilizationClaim(code: String, title: String): CappedUtilizationClaim {
    return CappedUtilizationClaim(
        code = code,
        title = title,
        carrier = "",
        margin = 0.0,
        rate = 0.0,
        ownerId = "",
        status = CappedUtilizationClaimStatus.REQUESTED,
    )
}

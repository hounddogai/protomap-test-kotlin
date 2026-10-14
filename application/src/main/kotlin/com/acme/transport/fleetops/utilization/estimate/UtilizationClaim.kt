package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization claim is in its life cycle. */
enum class UtilizationClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One utilization claim, with what the planners need to act on it. */
data class UtilizationClaim(
    var code: String,
    var name: String,
    var active: Boolean,
    var margin: Double,
    var cost: Double,
    var expiresAt: Int,
    var depot: String,
    var segment: String,
    var startsAt: Int,
    var archived: Boolean,
    var status: UtilizationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationClaimStatus): Boolean {
        when (status) {
            UtilizationClaimStatus.IDLE -> {
                when (next) {
                    UtilizationClaimStatus.WARMING, UtilizationClaimStatus.STOPPED -> return true
                }
                return false
            }
            UtilizationClaimStatus.WARMING -> return next == UtilizationClaimStatus.READY || next == UtilizationClaimStatus.STOPPED
            UtilizationClaimStatus.READY -> return next == UtilizationClaimStatus.STOPPED
            UtilizationClaimStatus.DRAINING -> return next == UtilizationClaimStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: UtilizationClaimStatus) {
        if (!canMoveTo(next)) {
            error("utilization claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netValue(): Double = round((cost + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): UtilizationClaim {
        return UtilizationClaim(
            code = code,
            name = name,
            active = active,
            margin = margin,
            cost = cost,
            expiresAt = expiresAt,
            depot = depot,
            segment = segment,
            startsAt = startsAt,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a utilization claim in status idle. */
fun newUtilizationClaim(code: String, name: String): UtilizationClaim {
    return UtilizationClaim(
        code = code,
        name = name,
        active = false,
        margin = 0.0,
        cost = 0.0,
        expiresAt = 0,
        depot = "",
        segment = "",
        startsAt = 0,
        archived = false,
        status = UtilizationClaimStatus.IDLE,
    )
}

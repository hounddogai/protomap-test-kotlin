package com.acme.transport.terminal.sortation.sortationsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling sortation claim is in its life cycle. */
enum class RollingSortationClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One rolling sortation claim, with what the planners need to act on it. */
data class RollingSortationClaim(
    var code: String,
    var label: String,
    var windowDays: Int,
    var timezone: String,
    var threshold: Double,
    var trailerId: String,
    var archived: Boolean,
    var status: RollingSortationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingSortationClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingSortationClaimStatus): Boolean {
        when (status) {
            RollingSortationClaimStatus.REQUESTED -> {
                when (next) {
                    RollingSortationClaimStatus.GRANTED, RollingSortationClaimStatus.EXPIRED -> return true
                }
                return false
            }
            RollingSortationClaimStatus.GRANTED -> {
                when (next) {
                    RollingSortationClaimStatus.REVOKED -> return true
                }
                return false
            }
            RollingSortationClaimStatus.REVOKED -> return next == RollingSortationClaimStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingSortationClaimStatus) {
        if (!canMoveTo(next)) {
            error("rolling sortation claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling sortation claim"} $code"

    fun effectiveTotal(): Double = round((threshold - windowDays.toDouble()) * 100) / 100

    fun snapshot(): RollingSortationClaim {
        return RollingSortationClaim(
            code = code,
            label = label,
            windowDays = windowDays,
            timezone = timezone,
            threshold = threshold,
            trailerId = trailerId,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling sortation claim in status requested. */
fun newRollingSortationClaim(code: String, label: String): RollingSortationClaim {
    return RollingSortationClaim(
        code = code,
        label = label,
        windowDays = 0,
        timezone = "",
        threshold = 0.0,
        trailerId = "",
        archived = false,
        status = RollingSortationClaimStatus.REQUESTED,
    )
}

package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending locker claim records of lastmile's locker work, and the rules, stores, and calculations around
// them.

/** Where a pending locker claim is in its life cycle. */
enum class PendingLockerClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending locker claim as the locker team keeps it. */
data class PendingLockerClaim(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var pinned: Boolean,
    var carrier: String,
    var tags: MutableList<String>,
    var channel: String,
    var updatedAt: Int,
    var status: PendingLockerClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingLockerClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingLockerClaimStatus): Boolean {
        when (status) {
            PendingLockerClaimStatus.IDLE -> {
                when (next) {
                    PendingLockerClaimStatus.DRAINING -> return true
                }
                return false
            }
            PendingLockerClaimStatus.WARMING -> return next == PendingLockerClaimStatus.DRAINING
            PendingLockerClaimStatus.READY -> {
                when (next) {
                    PendingLockerClaimStatus.STOPPED -> return true
                }
                return false
            }
            PendingLockerClaimStatus.DRAINING -> return next == PendingLockerClaimStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PendingLockerClaimStatus) {
        if (!canMoveTo(next)) {
            error("pending locker claim $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"pending locker claim"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedValue(): Double = round(fuelLiters * updatedAt.toDouble() * 100) / 100

    fun snapshot(): PendingLockerClaim {
        return PendingLockerClaim(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            pinned = pinned,
            carrier = carrier,
            tags = tags,
            channel = channel,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending locker claim in status idle. */
fun newPendingLockerClaim(code: String, title: String): PendingLockerClaim {
    return PendingLockerClaim(
        code = code,
        title = title,
        fuelLiters = 0.0,
        pinned = false,
        carrier = "",
        tags = mutableListOf<String>(),
        channel = "",
        updatedAt = 0,
        status = PendingLockerClaimStatus.IDLE,
    )
}

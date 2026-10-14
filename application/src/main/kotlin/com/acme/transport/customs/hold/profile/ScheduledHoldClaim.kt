package com.acme.transport.customs.hold.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled hold claim is in its life cycle. */
enum class ScheduledHoldClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a scheduled hold claim and its current status. */
data class ScheduledHoldClaim(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var windowDays: Int,
    var source: String,
    var expiresAt: Int,
    var stopCount: Int,
    var vehicleId: String,
    var status: ScheduledHoldClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHoldClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHoldClaimStatus): Boolean {
        when (status) {
            ScheduledHoldClaimStatus.IDLE -> return next == ScheduledHoldClaimStatus.STOPPED
            ScheduledHoldClaimStatus.WARMING -> {
                when (next) {
                    ScheduledHoldClaimStatus.DRAINING, ScheduledHoldClaimStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledHoldClaimStatus.READY -> {
                when (next) {
                    ScheduledHoldClaimStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledHoldClaimStatus.DRAINING -> {
                when (next) {
                    ScheduledHoldClaimStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledHoldClaimStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hold claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled hold claim"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedValue(): Double = round((stopCount.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): ScheduledHoldClaim {
        return ScheduledHoldClaim(
            code = code,
            name = name,
            tags = tags,
            windowDays = windowDays,
            source = source,
            expiresAt = expiresAt,
            stopCount = stopCount,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a scheduled hold claim in status idle. */
fun newScheduledHoldClaim(code: String, name: String): ScheduledHoldClaim {
    return ScheduledHoldClaim(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        windowDays = 0,
        source = "",
        expiresAt = 0,
        stopCount = 0,
        vehicleId = "",
        status = ScheduledHoldClaimStatus.IDLE,
    )
}

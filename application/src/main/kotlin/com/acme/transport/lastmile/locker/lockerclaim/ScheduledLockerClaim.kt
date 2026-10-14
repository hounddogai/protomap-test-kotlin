package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled locker claim records of lastmile's locker work, and the rules, stores, and calculations
// around them.

/** Where a scheduled locker claim is in its life cycle. */
enum class ScheduledLockerClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scheduled locker claim. Its code is unique within a tenant. */
data class ScheduledLockerClaim(
    var code: String,
    var name: String,
    var odometer: Int,
    var approved: Boolean,
    var updatedAt: Int,
    var stopCount: Int,
    var reviewer: String,
    var createdAt: Int,
    var category: String,
    var expiresAt: Int,
    var status: ScheduledLockerClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLockerClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLockerClaimStatus): Boolean {
        when (status) {
            ScheduledLockerClaimStatus.IDLE -> return next == ScheduledLockerClaimStatus.READY || next == ScheduledLockerClaimStatus.DRAINING
            ScheduledLockerClaimStatus.WARMING -> {
                when (next) {
                    ScheduledLockerClaimStatus.READY -> return true
                }
                return false
            }
            ScheduledLockerClaimStatus.READY -> return next == ScheduledLockerClaimStatus.DRAINING
            ScheduledLockerClaimStatus.DRAINING -> return next == ScheduledLockerClaimStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledLockerClaimStatus) {
        if (!canMoveTo(next)) {
            error("scheduled locker claim $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled locker claim"} $code"

    fun netValue(): Double = round((expiresAt.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): ScheduledLockerClaim {
        return ScheduledLockerClaim(
            code = code,
            name = name,
            odometer = odometer,
            approved = approved,
            updatedAt = updatedAt,
            stopCount = stopCount,
            reviewer = reviewer,
            createdAt = createdAt,
            category = category,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled locker claim in status idle. */
fun newScheduledLockerClaim(code: String, name: String): ScheduledLockerClaim {
    return ScheduledLockerClaim(
        code = code,
        name = name,
        odometer = 0,
        approved = false,
        updatedAt = 0,
        stopCount = 0,
        reviewer = "",
        createdAt = 0,
        category = "",
        expiresAt = 0,
        status = ScheduledLockerClaimStatus.IDLE,
    )
}

package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return window is in its life cycle. */
enum class LeaseReturnWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a lease return window and its current status. */
data class LeaseReturnWindow(
    var code: String,
    var label: String,
    var title: String,
    var durationMinutes: Int,
    var timezone: String,
    var floorValue: Double,
    var ownerId: String,
    var status: LeaseReturnWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnWindowStatus): Boolean {
        when (status) {
            LeaseReturnWindowStatus.DRAFT -> return next == LeaseReturnWindowStatus.ACTIVE || next == LeaseReturnWindowStatus.SUSPENDED
            LeaseReturnWindowStatus.PENDING -> return next == LeaseReturnWindowStatus.CLOSED
            LeaseReturnWindowStatus.ACTIVE -> {
                when (next) {
                    LeaseReturnWindowStatus.CLOSED -> return true
                }
                return false
            }
            LeaseReturnWindowStatus.SUSPENDED -> {
                when (next) {
                    LeaseReturnWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnWindowStatus) {
        if (!canMoveTo(next)) {
            error("lease return window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lease return window"} $code"

    fun projectedExposure(): Double = round(durationMinutes.toDouble() * floorValue * 100) / 100

    fun snapshot(): LeaseReturnWindow {
        return LeaseReturnWindow(
            code = code,
            label = label,
            title = title,
            durationMinutes = durationMinutes,
            timezone = timezone,
            floorValue = floorValue,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lease return window in status draft. */
fun newLeaseReturnWindow(code: String, label: String): LeaseReturnWindow {
    return LeaseReturnWindow(
        code = code,
        label = label,
        title = "",
        durationMinutes = 0,
        timezone = "",
        floorValue = 0.0,
        ownerId = "",
        status = LeaseReturnWindowStatus.DRAFT,
    )
}

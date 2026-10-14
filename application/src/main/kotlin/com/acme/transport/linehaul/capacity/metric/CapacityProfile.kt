package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity profile is in its life cycle. */
enum class CapacityProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One capacity profile, with what the planners need to act on it. */
data class CapacityProfile(
    var code: String,
    var name: String,
    var amount: Double,
    var windowDays: Int,
    var ceiling: Double,
    var status: CapacityProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityProfileStatus): Boolean {
        when (status) {
            CapacityProfileStatus.REQUESTED -> return next == CapacityProfileStatus.GRANTED
            CapacityProfileStatus.GRANTED -> return next == CapacityProfileStatus.REVOKED || next == CapacityProfileStatus.EXPIRED
            CapacityProfileStatus.REVOKED -> {
                when (next) {
                    CapacityProfileStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityProfileStatus) {
        if (!canMoveTo(next)) {
            error("capacity profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capacity profile"} $code"

    fun adjustedTotal(): Double = round((ceiling - windowDays.toDouble()) * 100) / 100

    fun snapshot(): CapacityProfile {
        return CapacityProfile(
            code = code,
            name = name,
            amount = amount,
            windowDays = windowDays,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capacity profile in status requested. */
fun newCapacityProfile(code: String, name: String): CapacityProfile {
    return CapacityProfile(
        code = code,
        name = name,
        amount = 0.0,
        windowDays = 0,
        ceiling = 0.0,
        status = CapacityProfileStatus.REQUESTED,
    )
}

package com.acme.transport.safety.violation.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending violation digest records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a pending violation digest is in its life cycle. */
enum class PendingViolationDigestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending violation digest as the violation team keeps it. */
data class PendingViolationDigest(
    var code: String,
    var title: String,
    var attempts: Int,
    var channel: String,
    var priority: Int,
    var stopCount: Int,
    var active: Boolean,
    var fuelLiters: Double,
    var durationMinutes: Int,
    var odometer: Int,
    var status: PendingViolationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingViolationDigestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingViolationDigestStatus): Boolean {
        when (status) {
            PendingViolationDigestStatus.PROPOSED -> return next == PendingViolationDigestStatus.APPROVED
            PendingViolationDigestStatus.APPROVED -> {
                when (next) {
                    PendingViolationDigestStatus.REJECTED, PendingViolationDigestStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingViolationDigestStatus.REJECTED -> {
                when (next) {
                    PendingViolationDigestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingViolationDigestStatus) {
        if (!canMoveTo(next)) {
            error("pending violation digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedLoad(): Double = round((attempts.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): PendingViolationDigest {
        return PendingViolationDigest(
            code = code,
            title = title,
            attempts = attempts,
            channel = channel,
            priority = priority,
            stopCount = stopCount,
            active = active,
            fuelLiters = fuelLiters,
            durationMinutes = durationMinutes,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending violation digest in status proposed. */
fun newPendingViolationDigest(code: String, title: String): PendingViolationDigest {
    return PendingViolationDigest(
        code = code,
        title = title,
        attempts = 0,
        channel = "",
        priority = 0,
        stopCount = 0,
        active = false,
        fuelLiters = 0.0,
        durationMinutes = 0,
        odometer = 0,
        status = PendingViolationDigestStatus.PROPOSED,
    )
}

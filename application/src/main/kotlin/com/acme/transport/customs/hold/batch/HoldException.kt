package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold exception is in its life cycle. */
enum class HoldExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One hold exception, with what the planners need to act on it. */
data class HoldException(
    var code: String,
    var label: String,
    var ownerId: String,
    var vehicleId: String,
    var channel: String,
    var version: Int,
    var attempts: Int,
    var threshold: Double,
    var status: HoldExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldExceptionStatus): Boolean {
        when (status) {
            HoldExceptionStatus.PLANNED -> return next == HoldExceptionStatus.IN_PROGRESS
            HoldExceptionStatus.SCHEDULED -> return next == HoldExceptionStatus.IN_PROGRESS || next == HoldExceptionStatus.CANCELLED
            HoldExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    HoldExceptionStatus.COMPLETED -> return true
                }
                return false
            }
            HoldExceptionStatus.COMPLETED -> return next == HoldExceptionStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: HoldExceptionStatus) {
        if (!canMoveTo(next)) {
            error("hold exception $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round((attempts.toDouble() + threshold) * 100) / 100

    fun snapshot(): HoldException {
        return HoldException(
            code = code,
            label = label,
            ownerId = ownerId,
            vehicleId = vehicleId,
            channel = channel,
            version = version,
            attempts = attempts,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a hold exception in status planned. */
fun newHoldException(code: String, label: String): HoldException {
    return HoldException(
        code = code,
        label = label,
        ownerId = "",
        vehicleId = "",
        channel = "",
        version = 0,
        attempts = 0,
        threshold = 0.0,
        status = HoldExceptionStatus.PLANNED,
    )
}

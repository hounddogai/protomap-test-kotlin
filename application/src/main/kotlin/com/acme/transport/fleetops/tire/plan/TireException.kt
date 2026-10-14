package com.acme.transport.fleetops.tire.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire exception is in its life cycle. */
enum class TireExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tire exception as the tire team keeps it. */
data class TireException(
    var code: String,
    var label: String,
    var channel: String,
    var approved: Boolean,
    var timezone: String,
    var region: String,
    var rate: Double,
    var attempts: Int,
    var status: TireExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireExceptionStatus): Boolean {
        when (status) {
            TireExceptionStatus.IDLE -> return next == TireExceptionStatus.READY || next == TireExceptionStatus.DRAINING
            TireExceptionStatus.WARMING -> {
                when (next) {
                    TireExceptionStatus.READY, TireExceptionStatus.DRAINING -> return true
                }
                return false
            }
            TireExceptionStatus.READY -> {
                when (next) {
                    TireExceptionStatus.STOPPED -> return true
                }
                return false
            }
            TireExceptionStatus.DRAINING -> {
                when (next) {
                    TireExceptionStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireExceptionStatus) {
        if (!canMoveTo(next)) {
            error("tire exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tire exception"} $code"

    fun netLoad(): Double = round((attempts.toDouble() - rate) * 100) / 100

    fun snapshot(): TireException {
        return TireException(
            code = code,
            label = label,
            channel = channel,
            approved = approved,
            timezone = timezone,
            region = region,
            rate = rate,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tire exception in status idle. */
fun newTireException(code: String, label: String): TireException {
    return TireException(
        code = code,
        label = label,
        channel = "",
        approved = false,
        timezone = "",
        region = "",
        rate = 0.0,
        attempts = 0,
        status = TireExceptionStatus.IDLE,
    )
}

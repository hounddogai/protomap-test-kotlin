package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled trailer exception records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a scheduled trailer exception is in its life cycle. */
enum class ScheduledTrailerExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One scheduled trailer exception, with what the planners need to act on it. */
data class ScheduledTrailerException(
    var code: String,
    var label: String,
    var externalRef: String,
    var windowDays: Int,
    var graceDays: Int,
    var unit: String,
    var checksum: String,
    var attempts: Int,
    var limit: Int,
    var margin: Double,
    var status: ScheduledTrailerExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTrailerExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTrailerExceptionStatus): Boolean {
        when (status) {
            ScheduledTrailerExceptionStatus.IDLE -> {
                when (next) {
                    ScheduledTrailerExceptionStatus.WARMING, ScheduledTrailerExceptionStatus.READY -> return true
                }
                return false
            }
            ScheduledTrailerExceptionStatus.WARMING -> {
                when (next) {
                    ScheduledTrailerExceptionStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledTrailerExceptionStatus.READY -> {
                when (next) {
                    ScheduledTrailerExceptionStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledTrailerExceptionStatus.DRAINING -> return next == ScheduledTrailerExceptionStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledTrailerExceptionStatus) {
        if (!canMoveTo(next)) {
            error("scheduled trailer exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled trailer exception"} $code"

    fun weightedLoad(): Double = round((attempts.toDouble() - margin) * 100) / 100

    fun snapshot(): ScheduledTrailerException {
        return ScheduledTrailerException(
            code = code,
            label = label,
            externalRef = externalRef,
            windowDays = windowDays,
            graceDays = graceDays,
            unit = unit,
            checksum = checksum,
            attempts = attempts,
            limit = limit,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled trailer exception in status idle. */
fun newScheduledTrailerException(code: String, label: String): ScheduledTrailerException {
    return ScheduledTrailerException(
        code = code,
        label = label,
        externalRef = "",
        windowDays = 0,
        graceDays = 0,
        unit = "",
        checksum = "",
        attempts = 0,
        limit = 0,
        margin = 0.0,
        status = ScheduledTrailerExceptionStatus.IDLE,
    )
}

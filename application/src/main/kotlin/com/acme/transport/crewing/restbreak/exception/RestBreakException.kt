package com.acme.transport.crewing.restbreak.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rest break exception records of crewing's rest break work, and the rules, stores, and calculations
// around them.

/** Where a rest break exception is in its life cycle. */
enum class RestBreakExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One rest break exception, with what the planners need to act on it. */
data class RestBreakException(
    var code: String,
    var label: String,
    var driverId: String,
    var startsAt: Int,
    var minimum: Double,
    var maximum: Double,
    var status: RestBreakExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakExceptionStatus): Boolean {
        when (status) {
            RestBreakExceptionStatus.PLANNED -> return next == RestBreakExceptionStatus.SCHEDULED
            RestBreakExceptionStatus.SCHEDULED -> return next == RestBreakExceptionStatus.COMPLETED
            RestBreakExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    RestBreakExceptionStatus.COMPLETED, RestBreakExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            RestBreakExceptionStatus.COMPLETED -> {
                when (next) {
                    RestBreakExceptionStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakExceptionStatus) {
        if (!canMoveTo(next)) {
            error("rest break exception $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netExposure(): Double = round((minimum - startsAt.toDouble()) * 100) / 100

    fun snapshot(): RestBreakException {
        return RestBreakException(
            code = code,
            label = label,
            driverId = driverId,
            startsAt = startsAt,
            minimum = minimum,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rest break exception in status planned. */
fun newRestBreakException(code: String, label: String): RestBreakException {
    return RestBreakException(
        code = code,
        label = label,
        driverId = "",
        startsAt = 0,
        minimum = 0.0,
        maximum = 0.0,
        status = RestBreakExceptionStatus.PLANNED,
    )
}

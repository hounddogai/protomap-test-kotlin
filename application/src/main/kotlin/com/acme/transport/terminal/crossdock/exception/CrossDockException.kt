package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cross dock exception records of terminal's cross dock work, and the rules, stores, and calculations
// around them.

/** Where a cross dock exception is in its life cycle. */
enum class CrossDockExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a cross dock exception and its current status. */
data class CrossDockException(
    var code: String,
    var name: String,
    var amount: Double,
    var reviewer: String,
    var durationMinutes: Int,
    var fuelLiters: Double,
    var ratio: Double,
    var stopCount: Int,
    var source: String,
    var country: String,
    var region: String,
    var status: CrossDockExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockExceptionStatus): Boolean {
        when (status) {
            CrossDockExceptionStatus.PLANNED -> return next == CrossDockExceptionStatus.CANCELLED
            CrossDockExceptionStatus.SCHEDULED -> {
                when (next) {
                    CrossDockExceptionStatus.COMPLETED -> return true
                }
                return false
            }
            CrossDockExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    CrossDockExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            CrossDockExceptionStatus.COMPLETED -> return next == CrossDockExceptionStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CrossDockExceptionStatus) {
        if (!canMoveTo(next)) {
            error("cross dock exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedVolume(): Double = round((stopCount.toDouble() - amount) * 100) / 100

    fun snapshot(): CrossDockException {
        return CrossDockException(
            code = code,
            name = name,
            amount = amount,
            reviewer = reviewer,
            durationMinutes = durationMinutes,
            fuelLiters = fuelLiters,
            ratio = ratio,
            stopCount = stopCount,
            source = source,
            country = country,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a cross dock exception in status planned. */
fun newCrossDockException(code: String, name: String): CrossDockException {
    return CrossDockException(
        code = code,
        name = name,
        amount = 0.0,
        reviewer = "",
        durationMinutes = 0,
        fuelLiters = 0.0,
        ratio = 0.0,
        stopCount = 0,
        source = "",
        country = "",
        region = "",
        status = CrossDockExceptionStatus.PLANNED,
    )
}

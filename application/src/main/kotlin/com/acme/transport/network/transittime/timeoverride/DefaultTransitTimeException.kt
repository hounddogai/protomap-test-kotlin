package com.acme.transport.network.transittime.timeoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default transit time exception is in its life cycle. */
enum class DefaultTransitTimeExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default transit time exception as the transit time team keeps it. */
data class DefaultTransitTimeException(
    var code: String,
    var name: String,
    var sequence: Int,
    var etaMinutes: Int,
    var unit: String,
    var version: Int,
    var minimum: Double,
    var dock: String,
    var status: DefaultTransitTimeExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTransitTimeExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTransitTimeExceptionStatus): Boolean {
        when (status) {
            DefaultTransitTimeExceptionStatus.IDLE -> {
                when (next) {
                    DefaultTransitTimeExceptionStatus.STOPPED -> return true
                }
                return false
            }
            DefaultTransitTimeExceptionStatus.WARMING -> {
                when (next) {
                    DefaultTransitTimeExceptionStatus.READY, DefaultTransitTimeExceptionStatus.STOPPED -> return true
                }
                return false
            }
            DefaultTransitTimeExceptionStatus.READY -> return next == DefaultTransitTimeExceptionStatus.DRAINING || next == DefaultTransitTimeExceptionStatus.STOPPED
            DefaultTransitTimeExceptionStatus.DRAINING -> {
                when (next) {
                    DefaultTransitTimeExceptionStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultTransitTimeExceptionStatus) {
        if (!canMoveTo(next)) {
            error("default transit time exception $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun effectiveExposure(): Double = round(version.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): DefaultTransitTimeException {
        return DefaultTransitTimeException(
            code = code,
            name = name,
            sequence = sequence,
            etaMinutes = etaMinutes,
            unit = unit,
            version = version,
            minimum = minimum,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a default transit time exception in status idle. */
fun newDefaultTransitTimeException(code: String, name: String): DefaultTransitTimeException {
    return DefaultTransitTimeException(
        code = code,
        name = name,
        sequence = 0,
        etaMinutes = 0,
        unit = "",
        version = 0,
        minimum = 0.0,
        dock = "",
        status = DefaultTransitTimeExceptionStatus.IDLE,
    )
}

package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default parcel exception records of lastmile's parcel work, and the rules, stores, and calculations
// around them.

/** Where a default parcel exception is in its life cycle. */
enum class DefaultParcelExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One default parcel exception, with what the planners need to act on it. */
data class DefaultParcelException(
    var code: String,
    var label: String,
    var margin: Double,
    var stopCount: Int,
    var currency: String,
    var attributes: MutableMap<String, String>,
    var sequence: Int,
    var timezone: String,
    var externalRef: String,
    var region: String,
    var startsAt: Int,
    var status: DefaultParcelExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultParcelExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultParcelExceptionStatus): Boolean {
        when (status) {
            DefaultParcelExceptionStatus.DRAFT -> return next == DefaultParcelExceptionStatus.PENDING || next == DefaultParcelExceptionStatus.CLOSED
            DefaultParcelExceptionStatus.PENDING -> return next == DefaultParcelExceptionStatus.ACTIVE
            DefaultParcelExceptionStatus.ACTIVE -> return next == DefaultParcelExceptionStatus.CLOSED
            DefaultParcelExceptionStatus.SUSPENDED -> return next == DefaultParcelExceptionStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DefaultParcelExceptionStatus) {
        if (!canMoveTo(next)) {
            error("default parcel exception $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round((margin + startsAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultParcelException {
        return DefaultParcelException(
            code = code,
            label = label,
            margin = margin,
            stopCount = stopCount,
            currency = currency,
            attributes = attributes,
            sequence = sequence,
            timezone = timezone,
            externalRef = externalRef,
            region = region,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default parcel exception in status draft. */
fun newDefaultParcelException(code: String, label: String): DefaultParcelException {
    return DefaultParcelException(
        code = code,
        label = label,
        margin = 0.0,
        stopCount = 0,
        currency = "",
        attributes = mutableMapOf<String, String>(),
        sequence = 0,
        timezone = "",
        externalRef = "",
        region = "",
        startsAt = 0,
        status = DefaultParcelExceptionStatus.DRAFT,
    )
}

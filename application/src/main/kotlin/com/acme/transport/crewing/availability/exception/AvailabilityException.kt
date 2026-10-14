package com.acme.transport.crewing.availability.exception

import com.acme.transport.kit.text.normalize

// The availability exception records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability exception is in its life cycle. */
enum class AvailabilityExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a availability exception and its current status. */
data class AvailabilityException(
    var code: String,
    var title: String,
    var label: String,
    var durationMinutes: Int,
    var region: String,
    var tags: MutableList<String>,
    var status: AvailabilityExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityExceptionStatus): Boolean {
        when (status) {
            AvailabilityExceptionStatus.DRAFT -> return next == AvailabilityExceptionStatus.CLOSED
            AvailabilityExceptionStatus.PENDING -> {
                when (next) {
                    AvailabilityExceptionStatus.ACTIVE, AvailabilityExceptionStatus.SUSPENDED -> return true
                }
                return false
            }
            AvailabilityExceptionStatus.ACTIVE -> {
                when (next) {
                    AvailabilityExceptionStatus.SUSPENDED -> return true
                }
                return false
            }
            AvailabilityExceptionStatus.SUSPENDED -> {
                when (next) {
                    AvailabilityExceptionStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityExceptionStatus) {
        if (!canMoveTo(next)) {
            error("availability exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): AvailabilityException {
        return AvailabilityException(
            code = code,
            title = title,
            label = label,
            durationMinutes = durationMinutes,
            region = region,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a availability exception in status draft. */
fun newAvailabilityException(code: String, title: String): AvailabilityException {
    return AvailabilityException(
        code = code,
        title = title,
        label = "",
        durationMinutes = 0,
        region = "",
        tags = mutableListOf<String>(),
        status = AvailabilityExceptionStatus.DRAFT,
    )
}

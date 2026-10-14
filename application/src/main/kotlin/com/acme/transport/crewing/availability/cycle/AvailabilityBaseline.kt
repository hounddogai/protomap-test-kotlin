package com.acme.transport.crewing.availability.cycle

import com.acme.transport.kit.text.normalize

/** Where a availability baseline is in its life cycle. */
enum class AvailabilityBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One availability baseline, with what the planners need to act on it. */
data class AvailabilityBaseline(
    var code: String,
    var title: String,
    var dock: String,
    var ratio: Double,
    var shiftCode: String,
    var status: AvailabilityBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityBaselineStatus): Boolean {
        when (status) {
            AvailabilityBaselineStatus.REQUESTED -> {
                when (next) {
                    AvailabilityBaselineStatus.GRANTED, AvailabilityBaselineStatus.EXPIRED -> return true
                }
                return false
            }
            AvailabilityBaselineStatus.GRANTED -> {
                when (next) {
                    AvailabilityBaselineStatus.REVOKED, AvailabilityBaselineStatus.EXPIRED -> return true
                }
                return false
            }
            AvailabilityBaselineStatus.REVOKED -> return next == AvailabilityBaselineStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AvailabilityBaselineStatus) {
        if (!canMoveTo(next)) {
            error("availability baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = ratio

    fun snapshot(): AvailabilityBaseline {
        return AvailabilityBaseline(
            code = code,
            title = title,
            dock = dock,
            ratio = ratio,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a availability baseline in status requested. */
fun newAvailabilityBaseline(code: String, title: String): AvailabilityBaseline {
    return AvailabilityBaseline(
        code = code,
        title = title,
        dock = "",
        ratio = 0.0,
        shiftCode = "",
        status = AvailabilityBaselineStatus.REQUESTED,
    )
}

package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.kit.text.normalize

/** Where a fuel burn segment is in its life cycle. */
enum class FuelBurnSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a fuel burn segment and its current status. */
data class FuelBurnSegment(
    var code: String,
    var label: String,
    var locale: String,
    var threshold: Double,
    var timezone: String,
    var channel: String,
    var source: String,
    var status: FuelBurnSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnSegmentStatus): Boolean {
        when (status) {
            FuelBurnSegmentStatus.OPEN -> {
                when (next) {
                    FuelBurnSegmentStatus.IN_REVIEW, FuelBurnSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            FuelBurnSegmentStatus.IN_REVIEW -> return next == FuelBurnSegmentStatus.ARCHIVED
            FuelBurnSegmentStatus.RESOLVED -> {
                when (next) {
                    FuelBurnSegmentStatus.REOPENED, FuelBurnSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            FuelBurnSegmentStatus.REOPENED -> {
                when (next) {
                    FuelBurnSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnSegmentStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = threshold

    fun snapshot(): FuelBurnSegment {
        return FuelBurnSegment(
            code = code,
            label = label,
            locale = locale,
            threshold = threshold,
            timezone = timezone,
            channel = channel,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fuel burn segment in status open. */
fun newFuelBurnSegment(code: String, label: String): FuelBurnSegment {
    return FuelBurnSegment(
        code = code,
        label = label,
        locale = "",
        threshold = 0.0,
        timezone = "",
        channel = "",
        source = "",
        status = FuelBurnSegmentStatus.OPEN,
    )
}

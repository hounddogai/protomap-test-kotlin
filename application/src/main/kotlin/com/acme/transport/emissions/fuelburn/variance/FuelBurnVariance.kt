package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel burn variance records of emissions's fuel burn work, and the rules, stores, and calculations
// around them.

/** Where a fuel burn variance is in its life cycle. */
enum class FuelBurnVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fuel burn variance, with what the planners need to act on it. */
data class FuelBurnVariance(
    var code: String,
    var title: String,
    var sequence: Int,
    var locale: String,
    var shiftCode: String,
    var timezone: String,
    var batchSize: Int,
    var segment: String,
    var maximum: Double,
    var graceDays: Int,
    var status: FuelBurnVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnVarianceStatus): Boolean {
        when (status) {
            FuelBurnVarianceStatus.IDLE -> return next == FuelBurnVarianceStatus.STOPPED
            FuelBurnVarianceStatus.WARMING -> return next == FuelBurnVarianceStatus.READY
            FuelBurnVarianceStatus.READY -> return next == FuelBurnVarianceStatus.STOPPED
            FuelBurnVarianceStatus.DRAINING -> return next == FuelBurnVarianceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FuelBurnVarianceStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveVolume(): Double = round((graceDays.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnVariance {
        return FuelBurnVariance(
            code = code,
            title = title,
            sequence = sequence,
            locale = locale,
            shiftCode = shiftCode,
            timezone = timezone,
            batchSize = batchSize,
            segment = segment,
            maximum = maximum,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fuel burn variance in status idle. */
fun newFuelBurnVariance(code: String, title: String): FuelBurnVariance {
    return FuelBurnVariance(
        code = code,
        title = title,
        sequence = 0,
        locale = "",
        shiftCode = "",
        timezone = "",
        batchSize = 0,
        segment = "",
        maximum = 0.0,
        graceDays = 0,
        status = FuelBurnVarianceStatus.IDLE,
    )
}

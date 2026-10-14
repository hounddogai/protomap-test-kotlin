package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending trailer yard adjustment is in its life cycle. */
enum class PendingTrailerYardAdjustmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a pending trailer yard adjustment and its current status. */
data class PendingTrailerYardAdjustment(
    var code: String,
    var label: String,
    var odometer: Int,
    var reviewer: String,
    var capacity: Int,
    var maximum: Double,
    var cost: Double,
    var fuelLiters: Double,
    var currency: String,
    var active: Boolean,
    var status: PendingTrailerYardAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTrailerYardAdjustmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTrailerYardAdjustmentStatus): Boolean {
        when (status) {
            PendingTrailerYardAdjustmentStatus.OPEN -> {
                when (next) {
                    PendingTrailerYardAdjustmentStatus.REOPENED -> return true
                }
                return false
            }
            PendingTrailerYardAdjustmentStatus.IN_REVIEW -> {
                when (next) {
                    PendingTrailerYardAdjustmentStatus.RESOLVED, PendingTrailerYardAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingTrailerYardAdjustmentStatus.RESOLVED -> {
                when (next) {
                    PendingTrailerYardAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingTrailerYardAdjustmentStatus.REOPENED -> {
                when (next) {
                    PendingTrailerYardAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingTrailerYardAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("pending trailer yard adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending trailer yard adjustment"} $code"

    fun adjustedValue(): Double = round((odometer.toDouble() + cost) * 100) / 100

    fun snapshot(): PendingTrailerYardAdjustment {
        return PendingTrailerYardAdjustment(
            code = code,
            label = label,
            odometer = odometer,
            reviewer = reviewer,
            capacity = capacity,
            maximum = maximum,
            cost = cost,
            fuelLiters = fuelLiters,
            currency = currency,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending trailer yard adjustment in status open. */
fun newPendingTrailerYardAdjustment(code: String, label: String): PendingTrailerYardAdjustment {
    return PendingTrailerYardAdjustment(
        code = code,
        label = label,
        odometer = 0,
        reviewer = "",
        capacity = 0,
        maximum = 0.0,
        cost = 0.0,
        fuelLiters = 0.0,
        currency = "",
        active = false,
        status = PendingTrailerYardAdjustmentStatus.OPEN,
    )
}

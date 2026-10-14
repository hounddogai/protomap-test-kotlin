package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy parcel adjustment is in its life cycle. */
enum class LegacyParcelAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy parcel adjustment as the parcel team keeps it. */
data class LegacyParcelAdjustment(
    var code: String,
    var name: String,
    var attempts: Int,
    var channel: String,
    var visible: Boolean,
    var pallets: Int,
    var trailerId: String,
    var summary: String,
    var budget: Double,
    var unit: String,
    var status: LegacyParcelAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyParcelAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyParcelAdjustmentStatus): Boolean {
        when (status) {
            LegacyParcelAdjustmentStatus.NEW -> {
                when (next) {
                    LegacyParcelAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            LegacyParcelAdjustmentStatus.QUEUED -> return next == LegacyParcelAdjustmentStatus.RUNNING
            LegacyParcelAdjustmentStatus.RUNNING -> {
                when (next) {
                    LegacyParcelAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            LegacyParcelAdjustmentStatus.DONE -> {
                when (next) {
                    LegacyParcelAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyParcelAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("legacy parcel adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedTotal(): Double = round((budget + attempts.toDouble()) * 100) / 100

    fun snapshot(): LegacyParcelAdjustment {
        return LegacyParcelAdjustment(
            code = code,
            name = name,
            attempts = attempts,
            channel = channel,
            visible = visible,
            pallets = pallets,
            trailerId = trailerId,
            summary = summary,
            budget = budget,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy parcel adjustment in status new. */
fun newLegacyParcelAdjustment(code: String, name: String): LegacyParcelAdjustment {
    return LegacyParcelAdjustment(
        code = code,
        name = name,
        attempts = 0,
        channel = "",
        visible = false,
        pallets = 0,
        trailerId = "",
        summary = "",
        budget = 0.0,
        unit = "",
        status = LegacyParcelAdjustmentStatus.NEW,
    )
}

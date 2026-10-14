package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted parcel override is in its life cycle. */
enum class WeightedParcelOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a weighted parcel override and its current status. */
data class WeightedParcelOverride(
    var code: String,
    var name: String,
    var batchSize: Int,
    var pallets: Int,
    var visible: Boolean,
    var attempts: Int,
    var status: WeightedParcelOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedParcelOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedParcelOverrideStatus): Boolean {
        when (status) {
            WeightedParcelOverrideStatus.DRAFT -> {
                when (next) {
                    WeightedParcelOverrideStatus.ACTIVE, WeightedParcelOverrideStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedParcelOverrideStatus.PENDING -> return next == WeightedParcelOverrideStatus.SUSPENDED || next == WeightedParcelOverrideStatus.CLOSED
            WeightedParcelOverrideStatus.ACTIVE -> return next == WeightedParcelOverrideStatus.SUSPENDED || next == WeightedParcelOverrideStatus.CLOSED
            WeightedParcelOverrideStatus.SUSPENDED -> return next == WeightedParcelOverrideStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: WeightedParcelOverrideStatus) {
        if (!canMoveTo(next)) {
            error("weighted parcel override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted parcel override"} $code"

    fun effectiveExposure(): Double = round((attempts.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): WeightedParcelOverride {
        return WeightedParcelOverride(
            code = code,
            name = name,
            batchSize = batchSize,
            pallets = pallets,
            visible = visible,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted parcel override in status draft. */
fun newWeightedParcelOverride(code: String, name: String): WeightedParcelOverride {
    return WeightedParcelOverride(
        code = code,
        name = name,
        batchSize = 0,
        pallets = 0,
        visible = false,
        attempts = 0,
        status = WeightedParcelOverrideStatus.DRAFT,
    )
}

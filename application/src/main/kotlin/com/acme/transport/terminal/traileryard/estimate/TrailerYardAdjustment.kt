package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard adjustment is in its life cycle. */
enum class TrailerYardAdjustmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a trailer yard adjustment and its current status. */
data class TrailerYardAdjustment(
    var code: String,
    var name: String,
    var odometer: Int,
    var floorValue: Double,
    var archived: Boolean,
    var threshold: Double,
    var revision: Int,
    var description: String,
    var status: TrailerYardAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardAdjustmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardAdjustmentStatus): Boolean {
        when (status) {
            TrailerYardAdjustmentStatus.DRAFT -> return next == TrailerYardAdjustmentStatus.ACTIVE
            TrailerYardAdjustmentStatus.PENDING -> return next == TrailerYardAdjustmentStatus.CLOSED
            TrailerYardAdjustmentStatus.ACTIVE -> {
                when (next) {
                    TrailerYardAdjustmentStatus.SUSPENDED -> return true
                }
                return false
            }
            TrailerYardAdjustmentStatus.SUSPENDED -> return next == TrailerYardAdjustmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TrailerYardAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard adjustment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"trailer yard adjustment"} $code"

    fun effectiveVolume(): Double = round((threshold + revision.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardAdjustment {
        return TrailerYardAdjustment(
            code = code,
            name = name,
            odometer = odometer,
            floorValue = floorValue,
            archived = archived,
            threshold = threshold,
            revision = revision,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a trailer yard adjustment in status draft. */
fun newTrailerYardAdjustment(code: String, name: String): TrailerYardAdjustment {
    return TrailerYardAdjustment(
        code = code,
        name = name,
        odometer = 0,
        floorValue = 0.0,
        archived = false,
        threshold = 0.0,
        revision = 0,
        description = "",
        status = TrailerYardAdjustmentStatus.DRAFT,
    )
}

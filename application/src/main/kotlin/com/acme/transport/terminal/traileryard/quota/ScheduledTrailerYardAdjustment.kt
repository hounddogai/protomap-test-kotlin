package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled trailer yard adjustment is in its life cycle. */
enum class ScheduledTrailerYardAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scheduled trailer yard adjustment, with what the planners need to act on it. */
data class ScheduledTrailerYardAdjustment(
    var code: String,
    var name: String,
    var ownerId: String,
    var discount: Double,
    var limit: Int,
    var dock: String,
    var segment: String,
    var carrier: String,
    var status: ScheduledTrailerYardAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTrailerYardAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTrailerYardAdjustmentStatus): Boolean {
        when (status) {
            ScheduledTrailerYardAdjustmentStatus.CREATED -> return next == ScheduledTrailerYardAdjustmentStatus.PUBLISHED
            ScheduledTrailerYardAdjustmentStatus.VALIDATED -> return next == ScheduledTrailerYardAdjustmentStatus.RETIRED
            ScheduledTrailerYardAdjustmentStatus.PUBLISHED -> {
                when (next) {
                    ScheduledTrailerYardAdjustmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledTrailerYardAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("scheduled trailer yard adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedValue(): Double = round(limit.toDouble() * discount * 100) / 100

    fun snapshot(): ScheduledTrailerYardAdjustment {
        return ScheduledTrailerYardAdjustment(
            code = code,
            name = name,
            ownerId = ownerId,
            discount = discount,
            limit = limit,
            dock = dock,
            segment = segment,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled trailer yard adjustment in status created. */
fun newScheduledTrailerYardAdjustment(code: String, name: String): ScheduledTrailerYardAdjustment {
    return ScheduledTrailerYardAdjustment(
        code = code,
        name = name,
        ownerId = "",
        discount = 0.0,
        limit = 0,
        dock = "",
        segment = "",
        carrier = "",
        status = ScheduledTrailerYardAdjustmentStatus.CREATED,
    )
}

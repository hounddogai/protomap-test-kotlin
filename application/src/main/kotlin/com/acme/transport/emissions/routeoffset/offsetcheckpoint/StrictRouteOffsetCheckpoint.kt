package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict route offset checkpoint records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a strict route offset checkpoint is in its life cycle. */
enum class StrictRouteOffsetCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One strict route offset checkpoint, with what the planners need to act on it. */
data class StrictRouteOffsetCheckpoint(
    var code: String,
    var title: String,
    var vehicleId: String,
    var loadKg: Double,
    var capacity: Int,
    var country: String,
    var graceDays: Int,
    var archived: Boolean,
    var discount: Double,
    var category: String,
    var status: StrictRouteOffsetCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRouteOffsetCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRouteOffsetCheckpointStatus): Boolean {
        when (status) {
            StrictRouteOffsetCheckpointStatus.OPEN -> {
                when (next) {
                    StrictRouteOffsetCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictRouteOffsetCheckpointStatus.IN_REVIEW -> return next == StrictRouteOffsetCheckpointStatus.RESOLVED || next == StrictRouteOffsetCheckpointStatus.REOPENED
            StrictRouteOffsetCheckpointStatus.RESOLVED -> {
                when (next) {
                    StrictRouteOffsetCheckpointStatus.REOPENED, StrictRouteOffsetCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictRouteOffsetCheckpointStatus.REOPENED -> {
                when (next) {
                    StrictRouteOffsetCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictRouteOffsetCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("strict route offset checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round((graceDays.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): StrictRouteOffsetCheckpoint {
        return StrictRouteOffsetCheckpoint(
            code = code,
            title = title,
            vehicleId = vehicleId,
            loadKg = loadKg,
            capacity = capacity,
            country = country,
            graceDays = graceDays,
            archived = archived,
            discount = discount,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict route offset checkpoint in status open. */
fun newStrictRouteOffsetCheckpoint(code: String, title: String): StrictRouteOffsetCheckpoint {
    return StrictRouteOffsetCheckpoint(
        code = code,
        title = title,
        vehicleId = "",
        loadKg = 0.0,
        capacity = 0,
        country = "",
        graceDays = 0,
        archived = false,
        discount = 0.0,
        category = "",
        status = StrictRouteOffsetCheckpointStatus.OPEN,
    )
}

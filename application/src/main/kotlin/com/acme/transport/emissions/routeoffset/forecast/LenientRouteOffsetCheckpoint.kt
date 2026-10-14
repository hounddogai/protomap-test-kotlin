package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient route offset checkpoint is in its life cycle. */
enum class LenientRouteOffsetCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lenient route offset checkpoint, with what the planners need to act on it. */
data class LenientRouteOffsetCheckpoint(
    var code: String,
    var title: String,
    var visible: Boolean,
    var description: String,
    var contactEmail: String,
    var threshold: Double,
    var ceiling: Double,
    var minimum: Double,
    var status: LenientRouteOffsetCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRouteOffsetCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRouteOffsetCheckpointStatus): Boolean {
        when (status) {
            LenientRouteOffsetCheckpointStatus.CREATED -> return next == LenientRouteOffsetCheckpointStatus.VALIDATED || next == LenientRouteOffsetCheckpointStatus.PUBLISHED
            LenientRouteOffsetCheckpointStatus.VALIDATED -> return next == LenientRouteOffsetCheckpointStatus.RETIRED
            LenientRouteOffsetCheckpointStatus.PUBLISHED -> return next == LenientRouteOffsetCheckpointStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientRouteOffsetCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("lenient route offset checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient route offset checkpoint"} $code"

    fun adjustedVolume(): Double = round(ceiling * threshold * 100) / 100

    fun snapshot(): LenientRouteOffsetCheckpoint {
        return LenientRouteOffsetCheckpoint(
            code = code,
            title = title,
            visible = visible,
            description = description,
            contactEmail = contactEmail,
            threshold = threshold,
            ceiling = ceiling,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient route offset checkpoint in status created. */
fun newLenientRouteOffsetCheckpoint(code: String, title: String): LenientRouteOffsetCheckpoint {
    return LenientRouteOffsetCheckpoint(
        code = code,
        title = title,
        visible = false,
        description = "",
        contactEmail = "",
        threshold = 0.0,
        ceiling = 0.0,
        minimum = 0.0,
        status = LenientRouteOffsetCheckpointStatus.CREATED,
    )
}

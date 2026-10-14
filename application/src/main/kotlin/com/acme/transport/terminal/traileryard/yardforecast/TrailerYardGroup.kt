package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard group is in its life cycle. */
enum class TrailerYardGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One trailer yard group, with what the planners need to act on it. */
data class TrailerYardGroup(
    var code: String,
    var name: String,
    var maximum: Double,
    var graceDays: Int,
    var reviewer: String,
    var status: TrailerYardGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardGroupStatus): Boolean {
        when (status) {
            TrailerYardGroupStatus.PLANNED -> return next == TrailerYardGroupStatus.IN_PROGRESS || next == TrailerYardGroupStatus.CANCELLED
            TrailerYardGroupStatus.SCHEDULED -> {
                when (next) {
                    TrailerYardGroupStatus.CANCELLED -> return true
                }
                return false
            }
            TrailerYardGroupStatus.IN_PROGRESS -> {
                when (next) {
                    TrailerYardGroupStatus.COMPLETED -> return true
                }
                return false
            }
            TrailerYardGroupStatus.COMPLETED -> {
                when (next) {
                    TrailerYardGroupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardGroupStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedVolume(): Double = round(graceDays.toDouble() * maximum * 100) / 100

    fun snapshot(): TrailerYardGroup {
        return TrailerYardGroup(
            code = code,
            name = name,
            maximum = maximum,
            graceDays = graceDays,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a trailer yard group in status planned. */
fun newTrailerYardGroup(code: String, name: String): TrailerYardGroup {
    return TrailerYardGroup(
        code = code,
        name = name,
        maximum = 0.0,
        graceDays = 0,
        reviewer = "",
        status = TrailerYardGroupStatus.PLANNED,
    )
}

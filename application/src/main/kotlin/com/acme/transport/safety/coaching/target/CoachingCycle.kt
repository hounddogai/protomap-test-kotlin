package com.acme.transport.safety.coaching.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching cycle is in its life cycle. */
enum class CoachingCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a coaching cycle and its current status. */
data class CoachingCycle(
    var code: String,
    var title: String,
    var minimum: Double,
    var unit: String,
    var contactEmail: String,
    var sequence: Int,
    var distanceKm: Double,
    var ratio: Double,
    var cost: Double,
    var notes: String,
    var maximum: Double,
    var status: CoachingCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingCycleStatus): Boolean {
        when (status) {
            CoachingCycleStatus.PLANNED -> {
                when (next) {
                    CoachingCycleStatus.IN_PROGRESS, CoachingCycleStatus.CANCELLED -> return true
                }
                return false
            }
            CoachingCycleStatus.SCHEDULED -> return next == CoachingCycleStatus.CANCELLED
            CoachingCycleStatus.IN_PROGRESS -> return next == CoachingCycleStatus.COMPLETED
            CoachingCycleStatus.COMPLETED -> {
                when (next) {
                    CoachingCycleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingCycleStatus) {
        if (!canMoveTo(next)) {
            error("coaching cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedTotal(): Double = round((distanceKm - sequence.toDouble()) * 100) / 100

    fun snapshot(): CoachingCycle {
        return CoachingCycle(
            code = code,
            title = title,
            minimum = minimum,
            unit = unit,
            contactEmail = contactEmail,
            sequence = sequence,
            distanceKm = distanceKm,
            ratio = ratio,
            cost = cost,
            notes = notes,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a coaching cycle in status planned. */
fun newCoachingCycle(code: String, title: String): CoachingCycle {
    return CoachingCycle(
        code = code,
        title = title,
        minimum = 0.0,
        unit = "",
        contactEmail = "",
        sequence = 0,
        distanceKm = 0.0,
        ratio = 0.0,
        cost = 0.0,
        notes = "",
        maximum = 0.0,
        status = CoachingCycleStatus.PLANNED,
    )
}

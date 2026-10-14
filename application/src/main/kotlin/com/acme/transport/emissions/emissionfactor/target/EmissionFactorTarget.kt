package com.acme.transport.emissions.emissionfactor.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The emission factor target records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a emission factor target is in its life cycle. */
enum class EmissionFactorTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One emission factor target, with what the planners need to act on it. */
data class EmissionFactorTarget(
    var code: String,
    var title: String,
    var rate: Double,
    var contactEmail: String,
    var label: String,
    var archived: Boolean,
    var startsAt: Int,
    var active: Boolean,
    var driverId: String,
    var lane: String,
    var status: EmissionFactorTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorTargetStatus): Boolean {
        when (status) {
            EmissionFactorTargetStatus.PLANNED -> return next == EmissionFactorTargetStatus.IN_PROGRESS || next == EmissionFactorTargetStatus.COMPLETED
            EmissionFactorTargetStatus.SCHEDULED -> return next == EmissionFactorTargetStatus.IN_PROGRESS
            EmissionFactorTargetStatus.IN_PROGRESS -> {
                when (next) {
                    EmissionFactorTargetStatus.COMPLETED -> return true
                }
                return false
            }
            EmissionFactorTargetStatus.COMPLETED -> return next == EmissionFactorTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: EmissionFactorTargetStatus) {
        if (!canMoveTo(next)) {
            error("emission factor target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($title)"

    fun effectiveValue(): Double = round((startsAt.toDouble() + rate) * 100) / 100

    fun snapshot(): EmissionFactorTarget {
        return EmissionFactorTarget(
            code = code,
            title = title,
            rate = rate,
            contactEmail = contactEmail,
            label = label,
            archived = archived,
            startsAt = startsAt,
            active = active,
            driverId = driverId,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a emission factor target in status planned. */
fun newEmissionFactorTarget(code: String, title: String): EmissionFactorTarget {
    return EmissionFactorTarget(
        code = code,
        title = title,
        rate = 0.0,
        contactEmail = "",
        label = "",
        archived = false,
        startsAt = 0,
        active = false,
        driverId = "",
        lane = "",
        status = EmissionFactorTargetStatus.PLANNED,
    )
}

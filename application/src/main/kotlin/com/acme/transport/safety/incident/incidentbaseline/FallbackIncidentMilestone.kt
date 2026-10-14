package com.acme.transport.safety.incident.incidentbaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback incident milestone is in its life cycle. */
enum class FallbackIncidentMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a fallback incident milestone and its current status. */
data class FallbackIncidentMilestone(
    var code: String,
    var title: String,
    var capacity: Int,
    var weight: Double,
    var active: Boolean,
    var distanceKm: Double,
    var score: Double,
    var maximum: Double,
    var archived: Boolean,
    var segment: String,
    var status: FallbackIncidentMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackIncidentMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackIncidentMilestoneStatus): Boolean {
        when (status) {
            FallbackIncidentMilestoneStatus.REQUESTED -> return next == FallbackIncidentMilestoneStatus.GRANTED || next == FallbackIncidentMilestoneStatus.EXPIRED
            FallbackIncidentMilestoneStatus.GRANTED -> {
                when (next) {
                    FallbackIncidentMilestoneStatus.REVOKED, FallbackIncidentMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackIncidentMilestoneStatus.REVOKED -> {
                when (next) {
                    FallbackIncidentMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackIncidentMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("fallback incident milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedTotal(): Double = round((maximum - capacity.toDouble()) * 100) / 100

    fun snapshot(): FallbackIncidentMilestone {
        return FallbackIncidentMilestone(
            code = code,
            title = title,
            capacity = capacity,
            weight = weight,
            active = active,
            distanceKm = distanceKm,
            score = score,
            maximum = maximum,
            archived = archived,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fallback incident milestone in status requested. */
fun newFallbackIncidentMilestone(code: String, title: String): FallbackIncidentMilestone {
    return FallbackIncidentMilestone(
        code = code,
        title = title,
        capacity = 0,
        weight = 0.0,
        active = false,
        distanceKm = 0.0,
        score = 0.0,
        maximum = 0.0,
        archived = false,
        segment = "",
        status = FallbackIncidentMilestoneStatus.REQUESTED,
    )
}

package com.acme.transport.safety.incident.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual incident segment is in its life cycle. */
enum class ManualIncidentSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual incident segment as the incident team keeps it. */
data class ManualIncidentSegment(
    var code: String,
    var label: String,
    var startsAt: Int,
    var pinned: Boolean,
    var distanceKm: Double,
    var budget: Double,
    var notes: String,
    var segment: String,
    var cost: Double,
    var description: String,
    var capacity: Int,
    var status: ManualIncidentSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualIncidentSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualIncidentSegmentStatus): Boolean {
        when (status) {
            ManualIncidentSegmentStatus.PROPOSED -> {
                when (next) {
                    ManualIncidentSegmentStatus.APPROVED, ManualIncidentSegmentStatus.REJECTED -> return true
                }
                return false
            }
            ManualIncidentSegmentStatus.APPROVED -> return next == ManualIncidentSegmentStatus.WITHDRAWN
            ManualIncidentSegmentStatus.REJECTED -> {
                when (next) {
                    ManualIncidentSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualIncidentSegmentStatus) {
        if (!canMoveTo(next)) {
            error("manual incident segment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round((capacity.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): ManualIncidentSegment {
        return ManualIncidentSegment(
            code = code,
            label = label,
            startsAt = startsAt,
            pinned = pinned,
            distanceKm = distanceKm,
            budget = budget,
            notes = notes,
            segment = segment,
            cost = cost,
            description = description,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a manual incident segment in status proposed. */
fun newManualIncidentSegment(code: String, label: String): ManualIncidentSegment {
    return ManualIncidentSegment(
        code = code,
        label = label,
        startsAt = 0,
        pinned = false,
        distanceKm = 0.0,
        budget = 0.0,
        notes = "",
        segment = "",
        cost = 0.0,
        description = "",
        capacity = 0,
        status = ManualIncidentSegmentStatus.PROPOSED,
    )
}

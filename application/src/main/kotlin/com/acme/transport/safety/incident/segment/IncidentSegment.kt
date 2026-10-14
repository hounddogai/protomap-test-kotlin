package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.text.normalize

// The incident segment records of safety's incident work, and the rules, stores, and calculations around
// them.

/** Where a incident segment is in its life cycle. */
enum class IncidentSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One incident segment, with what the planners need to act on it. */
data class IncidentSegment(
    var code: String,
    var label: String,
    var driverId: String,
    var cost: Double,
    var currency: String,
    var status: IncidentSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentSegmentStatus): Boolean {
        when (status) {
            IncidentSegmentStatus.PROPOSED -> return next == IncidentSegmentStatus.WITHDRAWN
            IncidentSegmentStatus.APPROVED -> {
                when (next) {
                    IncidentSegmentStatus.REJECTED -> return true
                }
                return false
            }
            IncidentSegmentStatus.REJECTED -> {
                when (next) {
                    IncidentSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentSegmentStatus) {
        if (!canMoveTo(next)) {
            error("incident segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"incident segment"} $code"

    fun measuredValue(): Double = cost

    fun snapshot(): IncidentSegment {
        return IncidentSegment(
            code = code,
            label = label,
            driverId = driverId,
            cost = cost,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a incident segment in status proposed. */
fun newIncidentSegment(code: String, label: String): IncidentSegment {
    return IncidentSegment(
        code = code,
        label = label,
        driverId = "",
        cost = 0.0,
        currency = "",
        status = IncidentSegmentStatus.PROPOSED,
    )
}

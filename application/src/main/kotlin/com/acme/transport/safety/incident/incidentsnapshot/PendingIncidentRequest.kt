package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.text.normalize

/** Where a pending incident request is in its life cycle. */
enum class PendingIncidentRequestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending incident request, with what the planners need to act on it. */
data class PendingIncidentRequest(
    var code: String,
    var title: String,
    var country: String,
    var timezone: String,
    var trailerId: String,
    var score: Double,
    var status: PendingIncidentRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingIncidentRequestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingIncidentRequestStatus): Boolean {
        when (status) {
            PendingIncidentRequestStatus.PLANNED -> return next == PendingIncidentRequestStatus.COMPLETED || next == PendingIncidentRequestStatus.CANCELLED
            PendingIncidentRequestStatus.SCHEDULED -> return next == PendingIncidentRequestStatus.COMPLETED || next == PendingIncidentRequestStatus.CANCELLED
            PendingIncidentRequestStatus.IN_PROGRESS -> {
                when (next) {
                    PendingIncidentRequestStatus.CANCELLED -> return true
                }
                return false
            }
            PendingIncidentRequestStatus.COMPLETED -> return next == PendingIncidentRequestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingIncidentRequestStatus) {
        if (!canMoveTo(next)) {
            error("pending incident request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = score

    fun snapshot(): PendingIncidentRequest {
        return PendingIncidentRequest(
            code = code,
            title = title,
            country = country,
            timezone = timezone,
            trailerId = trailerId,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending incident request in status planned. */
fun newPendingIncidentRequest(code: String, title: String): PendingIncidentRequest {
    return PendingIncidentRequest(
        code = code,
        title = title,
        country = "",
        timezone = "",
        trailerId = "",
        score = 0.0,
        status = PendingIncidentRequestStatus.PLANNED,
    )
}

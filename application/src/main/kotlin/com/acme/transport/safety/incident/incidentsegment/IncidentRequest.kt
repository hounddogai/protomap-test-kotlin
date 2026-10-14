package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident request is in its life cycle. */
enum class IncidentRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One incident request, with what the planners need to act on it. */
data class IncidentRequest(
    var code: String,
    var title: String,
    var phone: String,
    var distanceKm: Double,
    var windowDays: Int,
    var status: IncidentRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentRequestStatus): Boolean {
        when (status) {
            IncidentRequestStatus.DRAFT -> return next == IncidentRequestStatus.CLOSED
            IncidentRequestStatus.PENDING -> {
                when (next) {
                    IncidentRequestStatus.CLOSED -> return true
                }
                return false
            }
            IncidentRequestStatus.ACTIVE -> return next == IncidentRequestStatus.SUSPENDED || next == IncidentRequestStatus.CLOSED
            IncidentRequestStatus.SUSPENDED -> return next == IncidentRequestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: IncidentRequestStatus) {
        if (!canMoveTo(next)) {
            error("incident request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netTotal(): Double = round((distanceKm + windowDays.toDouble()) * 100) / 100

    fun snapshot(): IncidentRequest {
        return IncidentRequest(
            code = code,
            title = title,
            phone = phone,
            distanceKm = distanceKm,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a incident request in status draft. */
fun newIncidentRequest(code: String, title: String): IncidentRequest {
    return IncidentRequest(
        code = code,
        title = title,
        phone = "",
        distanceKm = 0.0,
        windowDays = 0,
        status = IncidentRequestStatus.DRAFT,
    )
}

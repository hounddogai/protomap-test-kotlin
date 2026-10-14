package com.acme.transport.fleetops.inspection.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection request is in its life cycle. */
enum class InspectionRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One inspection request, with what the planners need to act on it. */
data class InspectionRequest(
    var code: String,
    var title: String,
    var startsAt: Int,
    var label: String,
    var graceDays: Int,
    var routeCode: String,
    var contactEmail: String,
    var minimum: Double,
    var attempts: Int,
    var status: InspectionRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionRequestStatus): Boolean {
        when (status) {
            InspectionRequestStatus.CREATED -> {
                when (next) {
                    InspectionRequestStatus.RETIRED -> return true
                }
                return false
            }
            InspectionRequestStatus.VALIDATED -> return next == InspectionRequestStatus.PUBLISHED || next == InspectionRequestStatus.RETIRED
            InspectionRequestStatus.PUBLISHED -> return next == InspectionRequestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: InspectionRequestStatus) {
        if (!canMoveTo(next)) {
            error("inspection request $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"inspection request"} $code"

    fun adjustedExposure(): Double = round(graceDays.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): InspectionRequest {
        return InspectionRequest(
            code = code,
            title = title,
            startsAt = startsAt,
            label = label,
            graceDays = graceDays,
            routeCode = routeCode,
            contactEmail = contactEmail,
            minimum = minimum,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection request in status created. */
fun newInspectionRequest(code: String, title: String): InspectionRequest {
    return InspectionRequest(
        code = code,
        title = title,
        startsAt = 0,
        label = "",
        graceDays = 0,
        routeCode = "",
        contactEmail = "",
        minimum = 0.0,
        attempts = 0,
        status = InspectionRequestStatus.CREATED,
    )
}

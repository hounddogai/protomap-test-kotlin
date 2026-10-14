package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual service plan request is in its life cycle. */
enum class ManualServicePlanRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One manual service plan request, with what the planners need to act on it. */
data class ManualServicePlanRequest(
    var code: String,
    var title: String,
    var createdAt: Int,
    var distanceKm: Double,
    var summary: String,
    var expiresAt: Int,
    var region: String,
    var status: ManualServicePlanRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualServicePlanRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualServicePlanRequestStatus): Boolean {
        when (status) {
            ManualServicePlanRequestStatus.CREATED -> return next == ManualServicePlanRequestStatus.VALIDATED
            ManualServicePlanRequestStatus.VALIDATED -> {
                when (next) {
                    ManualServicePlanRequestStatus.RETIRED -> return true
                }
                return false
            }
            ManualServicePlanRequestStatus.PUBLISHED -> return next == ManualServicePlanRequestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualServicePlanRequestStatus) {
        if (!canMoveTo(next)) {
            error("manual service plan request $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedExposure(): Double = round(distanceKm * expiresAt.toDouble() * 100) / 100

    fun snapshot(): ManualServicePlanRequest {
        return ManualServicePlanRequest(
            code = code,
            title = title,
            createdAt = createdAt,
            distanceKm = distanceKm,
            summary = summary,
            expiresAt = expiresAt,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual service plan request in status created. */
fun newManualServicePlanRequest(code: String, title: String): ManualServicePlanRequest {
    return ManualServicePlanRequest(
        code = code,
        title = title,
        createdAt = 0,
        distanceKm = 0.0,
        summary = "",
        expiresAt = 0,
        region = "",
        status = ManualServicePlanRequestStatus.CREATED,
    )
}

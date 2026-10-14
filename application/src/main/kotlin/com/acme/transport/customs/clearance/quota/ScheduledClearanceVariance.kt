package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled clearance variance is in its life cycle. */
enum class ScheduledClearanceVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scheduled clearance variance. Its code is unique within a tenant. */
data class ScheduledClearanceVariance(
    var code: String,
    var title: String,
    var weight: Double,
    var shiftCode: String,
    var name: String,
    var summary: String,
    var distanceKm: Double,
    var pinned: Boolean,
    var routeCode: String,
    var status: ScheduledClearanceVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledClearanceVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledClearanceVarianceStatus): Boolean {
        when (status) {
            ScheduledClearanceVarianceStatus.PLANNED -> return next == ScheduledClearanceVarianceStatus.IN_PROGRESS || next == ScheduledClearanceVarianceStatus.CANCELLED
            ScheduledClearanceVarianceStatus.SCHEDULED -> {
                when (next) {
                    ScheduledClearanceVarianceStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ScheduledClearanceVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledClearanceVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledClearanceVarianceStatus.COMPLETED -> return next == ScheduledClearanceVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledClearanceVarianceStatus) {
        if (!canMoveTo(next)) {
            error("scheduled clearance variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round(weight * distanceKm * 100) / 100

    fun snapshot(): ScheduledClearanceVariance {
        return ScheduledClearanceVariance(
            code = code,
            title = title,
            weight = weight,
            shiftCode = shiftCode,
            name = name,
            summary = summary,
            distanceKm = distanceKm,
            pinned = pinned,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled clearance variance in status planned. */
fun newScheduledClearanceVariance(code: String, title: String): ScheduledClearanceVariance {
    return ScheduledClearanceVariance(
        code = code,
        title = title,
        weight = 0.0,
        shiftCode = "",
        name = "",
        summary = "",
        distanceKm = 0.0,
        pinned = false,
        routeCode = "",
        status = ScheduledClearanceVarianceStatus.PLANNED,
    )
}

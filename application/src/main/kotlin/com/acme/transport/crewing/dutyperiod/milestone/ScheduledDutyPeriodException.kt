package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled duty period exception is in its life cycle. */
enum class ScheduledDutyPeriodExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One scheduled duty period exception, with what the planners need to act on it. */
data class ScheduledDutyPeriodException(
    var code: String,
    var name: String,
    var trailerId: String,
    var odometer: Int,
    var region: String,
    var attributes: MutableMap<String, String>,
    var depot: String,
    var dock: String,
    var reviewer: String,
    var distanceKm: Double,
    var phone: String,
    var status: ScheduledDutyPeriodExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDutyPeriodExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDutyPeriodExceptionStatus): Boolean {
        when (status) {
            ScheduledDutyPeriodExceptionStatus.PLANNED -> {
                when (next) {
                    ScheduledDutyPeriodExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledDutyPeriodExceptionStatus.SCHEDULED -> return next == ScheduledDutyPeriodExceptionStatus.IN_PROGRESS
            ScheduledDutyPeriodExceptionStatus.IN_PROGRESS -> return next == ScheduledDutyPeriodExceptionStatus.COMPLETED || next == ScheduledDutyPeriodExceptionStatus.CANCELLED
            ScheduledDutyPeriodExceptionStatus.COMPLETED -> return next == ScheduledDutyPeriodExceptionStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledDutyPeriodExceptionStatus) {
        if (!canMoveTo(next)) {
            error("scheduled duty period exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled duty period exception"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round(distanceKm * odometer.toDouble() * 100) / 100

    fun snapshot(): ScheduledDutyPeriodException {
        return ScheduledDutyPeriodException(
            code = code,
            name = name,
            trailerId = trailerId,
            odometer = odometer,
            region = region,
            attributes = attributes,
            depot = depot,
            dock = dock,
            reviewer = reviewer,
            distanceKm = distanceKm,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled duty period exception in status planned. */
fun newScheduledDutyPeriodException(code: String, name: String): ScheduledDutyPeriodException {
    return ScheduledDutyPeriodException(
        code = code,
        name = name,
        trailerId = "",
        odometer = 0,
        region = "",
        attributes = mutableMapOf<String, String>(),
        depot = "",
        dock = "",
        reviewer = "",
        distanceKm = 0.0,
        phone = "",
        status = ScheduledDutyPeriodExceptionStatus.PLANNED,
    )
}

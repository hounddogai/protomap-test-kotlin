package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization schedule is in its life cycle. */
enum class UtilizationScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A utilization schedule as the utilization team keeps it. */
data class UtilizationSchedule(
    var code: String,
    var name: String,
    var carrier: String,
    var limit: Int,
    var capacity: Int,
    var externalRef: String,
    var distanceKm: Double,
    var lane: String,
    var status: UtilizationScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationScheduleStatus): Boolean {
        when (status) {
            UtilizationScheduleStatus.DRAFT -> return next == UtilizationScheduleStatus.PENDING
            UtilizationScheduleStatus.PENDING -> return next == UtilizationScheduleStatus.SUSPENDED || next == UtilizationScheduleStatus.CLOSED
            UtilizationScheduleStatus.ACTIVE -> {
                when (next) {
                    UtilizationScheduleStatus.CLOSED -> return true
                }
                return false
            }
            UtilizationScheduleStatus.SUSPENDED -> return next == UtilizationScheduleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: UtilizationScheduleStatus) {
        if (!canMoveTo(next)) {
            error("utilization schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"utilization schedule"} $code"

    fun projectedValue(): Double = round((distanceKm - limit.toDouble()) * 100) / 100

    fun snapshot(): UtilizationSchedule {
        return UtilizationSchedule(
            code = code,
            name = name,
            carrier = carrier,
            limit = limit,
            capacity = capacity,
            externalRef = externalRef,
            distanceKm = distanceKm,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a utilization schedule in status draft. */
fun newUtilizationSchedule(code: String, name: String): UtilizationSchedule {
    return UtilizationSchedule(
        code = code,
        name = name,
        carrier = "",
        limit = 0,
        capacity = 0,
        externalRef = "",
        distanceKm = 0.0,
        lane = "",
        status = UtilizationScheduleStatus.DRAFT,
    )
}

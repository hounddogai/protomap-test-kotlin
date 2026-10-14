package com.acme.transport.fleetops.serviceplan.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan schedule is in its life cycle. */
enum class ServicePlanScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A service plan schedule as the service plan team keeps it. */
data class ServicePlanSchedule(
    var code: String,
    var name: String,
    var archived: Boolean,
    var locale: String,
    var attempts: Int,
    var distanceKm: Double,
    var shiftCode: String,
    var weight: Double,
    var carrier: String,
    var title: String,
    var channel: String,
    var status: ServicePlanScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanScheduleStatus): Boolean {
        when (status) {
            ServicePlanScheduleStatus.IDLE -> return next == ServicePlanScheduleStatus.WARMING || next == ServicePlanScheduleStatus.READY
            ServicePlanScheduleStatus.WARMING -> return next == ServicePlanScheduleStatus.STOPPED
            ServicePlanScheduleStatus.READY -> return next == ServicePlanScheduleStatus.DRAINING
            ServicePlanScheduleStatus.DRAINING -> return next == ServicePlanScheduleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ServicePlanScheduleStatus) {
        if (!canMoveTo(next)) {
            error("service plan schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedExposure(): Double = round((distanceKm - attempts.toDouble()) * 100) / 100

    fun snapshot(): ServicePlanSchedule {
        return ServicePlanSchedule(
            code = code,
            name = name,
            archived = archived,
            locale = locale,
            attempts = attempts,
            distanceKm = distanceKm,
            shiftCode = shiftCode,
            weight = weight,
            carrier = carrier,
            title = title,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a service plan schedule in status idle. */
fun newServicePlanSchedule(code: String, name: String): ServicePlanSchedule {
    return ServicePlanSchedule(
        code = code,
        name = name,
        archived = false,
        locale = "",
        attempts = 0,
        distanceKm = 0.0,
        shiftCode = "",
        weight = 0.0,
        carrier = "",
        title = "",
        channel = "",
        status = ServicePlanScheduleStatus.IDLE,
    )
}

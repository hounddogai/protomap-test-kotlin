package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area schedule is in its life cycle. */
enum class ServiceAreaScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a service area schedule and its current status. */
data class ServiceAreaSchedule(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var updatedAt: Int,
    var currency: String,
    var timezone: String,
    var carrier: String,
    var driverId: String,
    var region: String,
    var threshold: Double,
    var batchSize: Int,
    var status: ServiceAreaScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaScheduleStatus): Boolean {
        when (status) {
            ServiceAreaScheduleStatus.DRAFT -> return next == ServiceAreaScheduleStatus.PENDING || next == ServiceAreaScheduleStatus.CLOSED
            ServiceAreaScheduleStatus.PENDING -> {
                when (next) {
                    ServiceAreaScheduleStatus.SUSPENDED -> return true
                }
                return false
            }
            ServiceAreaScheduleStatus.ACTIVE -> {
                when (next) {
                    ServiceAreaScheduleStatus.SUSPENDED, ServiceAreaScheduleStatus.CLOSED -> return true
                }
                return false
            }
            ServiceAreaScheduleStatus.SUSPENDED -> return next == ServiceAreaScheduleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ServiceAreaScheduleStatus) {
        if (!canMoveTo(next)) {
            error("service area schedule $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"service area schedule"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round((batchSize.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaSchedule {
        return ServiceAreaSchedule(
            code = code,
            title = title,
            tags = tags,
            updatedAt = updatedAt,
            currency = currency,
            timezone = timezone,
            carrier = carrier,
            driverId = driverId,
            region = region,
            threshold = threshold,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a service area schedule in status draft. */
fun newServiceAreaSchedule(code: String, title: String): ServiceAreaSchedule {
    return ServiceAreaSchedule(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        updatedAt = 0,
        currency = "",
        timezone = "",
        carrier = "",
        driverId = "",
        region = "",
        threshold = 0.0,
        batchSize = 0,
        status = ServiceAreaScheduleStatus.DRAFT,
    )
}

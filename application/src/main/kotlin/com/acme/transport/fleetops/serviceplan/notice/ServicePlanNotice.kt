package com.acme.transport.fleetops.serviceplan.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service plan notice records of fleetops's service plan work, and the rules, stores, and calculations
// around them.

/** Where a service plan notice is in its life cycle. */
enum class ServicePlanNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One service plan notice, with what the planners need to act on it. */
data class ServicePlanNotice(
    var code: String,
    var name: String,
    var lane: String,
    var updatedAt: Int,
    var rate: Double,
    var margin: Double,
    var batchSize: Int,
    var phone: String,
    var stopCount: Int,
    var dock: String,
    var status: ServicePlanNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanNoticeStatus): Boolean {
        when (status) {
            ServicePlanNoticeStatus.PLANNED -> {
                when (next) {
                    ServicePlanNoticeStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ServicePlanNoticeStatus.SCHEDULED -> return next == ServicePlanNoticeStatus.IN_PROGRESS
            ServicePlanNoticeStatus.IN_PROGRESS -> {
                when (next) {
                    ServicePlanNoticeStatus.COMPLETED, ServicePlanNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            ServicePlanNoticeStatus.COMPLETED -> {
                when (next) {
                    ServicePlanNoticeStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanNoticeStatus) {
        if (!canMoveTo(next)) {
            error("service plan notice $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedExposure(): Double = round((updatedAt.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): ServicePlanNotice {
        return ServicePlanNotice(
            code = code,
            name = name,
            lane = lane,
            updatedAt = updatedAt,
            rate = rate,
            margin = margin,
            batchSize = batchSize,
            phone = phone,
            stopCount = stopCount,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a service plan notice in status planned. */
fun newServicePlanNotice(code: String, name: String): ServicePlanNotice {
    return ServicePlanNotice(
        code = code,
        name = name,
        lane = "",
        updatedAt = 0,
        rate = 0.0,
        margin = 0.0,
        batchSize = 0,
        phone = "",
        stopCount = 0,
        dock = "",
        status = ServicePlanNoticeStatus.PLANNED,
    )
}

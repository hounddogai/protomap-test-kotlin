package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service area notice records of network's service area work, and the rules, stores, and calculations
// around them.

/** Where a service area notice is in its life cycle. */
enum class ServiceAreaNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A service area notice as the service area team keeps it. */
data class ServiceAreaNotice(
    var code: String,
    var name: String,
    var minimum: Double,
    var stopCount: Int,
    var routeCode: String,
    var notes: String,
    var etaMinutes: Int,
    var visible: Boolean,
    var status: ServiceAreaNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaNoticeStatus): Boolean {
        when (status) {
            ServiceAreaNoticeStatus.IDLE -> return next == ServiceAreaNoticeStatus.READY
            ServiceAreaNoticeStatus.WARMING -> {
                when (next) {
                    ServiceAreaNoticeStatus.STOPPED -> return true
                }
                return false
            }
            ServiceAreaNoticeStatus.READY -> return next == ServiceAreaNoticeStatus.STOPPED
            ServiceAreaNoticeStatus.DRAINING -> {
                when (next) {
                    ServiceAreaNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaNoticeStatus) {
        if (!canMoveTo(next)) {
            error("service area notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"service area notice"} $code"

    fun effectiveLoad(): Double = round((minimum - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaNotice {
        return ServiceAreaNotice(
            code = code,
            name = name,
            minimum = minimum,
            stopCount = stopCount,
            routeCode = routeCode,
            notes = notes,
            etaMinutes = etaMinutes,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service area notice in status idle. */
fun newServiceAreaNotice(code: String, name: String): ServiceAreaNotice {
    return ServiceAreaNotice(
        code = code,
        name = name,
        minimum = 0.0,
        stopCount = 0,
        routeCode = "",
        notes = "",
        etaMinutes = 0,
        visible = false,
        status = ServiceAreaNoticeStatus.IDLE,
    )
}

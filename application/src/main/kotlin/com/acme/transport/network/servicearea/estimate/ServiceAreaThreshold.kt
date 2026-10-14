package com.acme.transport.network.servicearea.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area threshold is in its life cycle. */
enum class ServiceAreaThresholdStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A service area threshold as the service area team keeps it. */
data class ServiceAreaThreshold(
    var code: String,
    var name: String,
    var windowDays: Int,
    var retries: Int,
    var routeCode: String,
    var createdAt: Int,
    var limit: Int,
    var status: ServiceAreaThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaThresholdStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaThresholdStatus): Boolean {
        when (status) {
            ServiceAreaThresholdStatus.DRAFT -> return next == ServiceAreaThresholdStatus.SUSPENDED
            ServiceAreaThresholdStatus.PENDING -> {
                when (next) {
                    ServiceAreaThresholdStatus.CLOSED -> return true
                }
                return false
            }
            ServiceAreaThresholdStatus.ACTIVE -> return next == ServiceAreaThresholdStatus.SUSPENDED || next == ServiceAreaThresholdStatus.CLOSED
            ServiceAreaThresholdStatus.SUSPENDED -> return next == ServiceAreaThresholdStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ServiceAreaThresholdStatus) {
        if (!canMoveTo(next)) {
            error("service area threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveTotal(): Double = round(createdAt.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): ServiceAreaThreshold {
        return ServiceAreaThreshold(
            code = code,
            name = name,
            windowDays = windowDays,
            retries = retries,
            routeCode = routeCode,
            createdAt = createdAt,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service area threshold in status draft. */
fun newServiceAreaThreshold(code: String, name: String): ServiceAreaThreshold {
    return ServiceAreaThreshold(
        code = code,
        name = name,
        windowDays = 0,
        retries = 0,
        routeCode = "",
        createdAt = 0,
        limit = 0,
        status = ServiceAreaThresholdStatus.DRAFT,
    )
}

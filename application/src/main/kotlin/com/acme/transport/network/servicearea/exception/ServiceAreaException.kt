package com.acme.transport.network.servicearea.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service area exception records of network's service area work, and the rules, stores, and calculations
// around them.

/** Where a service area exception is in its life cycle. */
enum class ServiceAreaExceptionStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A service area exception as the service area team keeps it. */
data class ServiceAreaException(
    var code: String,
    var title: String,
    var retries: Int,
    var minimum: Double,
    var currency: String,
    var region: String,
    var cost: Double,
    var version: Int,
    var startsAt: Int,
    var pinned: Boolean,
    var durationMinutes: Int,
    var status: ServiceAreaExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaExceptionStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaExceptionStatus): Boolean {
        when (status) {
            ServiceAreaExceptionStatus.OPEN -> {
                when (next) {
                    ServiceAreaExceptionStatus.IN_REVIEW -> return true
                }
                return false
            }
            ServiceAreaExceptionStatus.IN_REVIEW -> return next == ServiceAreaExceptionStatus.REOPENED || next == ServiceAreaExceptionStatus.ARCHIVED
            ServiceAreaExceptionStatus.RESOLVED -> {
                when (next) {
                    ServiceAreaExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
            ServiceAreaExceptionStatus.REOPENED -> {
                when (next) {
                    ServiceAreaExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaExceptionStatus) {
        if (!canMoveTo(next)) {
            error("service area exception $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun netExposure(): Double = round((startsAt.toDouble() - cost) * 100) / 100

    fun snapshot(): ServiceAreaException {
        return ServiceAreaException(
            code = code,
            title = title,
            retries = retries,
            minimum = minimum,
            currency = currency,
            region = region,
            cost = cost,
            version = version,
            startsAt = startsAt,
            pinned = pinned,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a service area exception in status open. */
fun newServiceAreaException(code: String, title: String): ServiceAreaException {
    return ServiceAreaException(
        code = code,
        title = title,
        retries = 0,
        minimum = 0.0,
        currency = "",
        region = "",
        cost = 0.0,
        version = 0,
        startsAt = 0,
        pinned = false,
        durationMinutes = 0,
        status = ServiceAreaExceptionStatus.OPEN,
    )
}

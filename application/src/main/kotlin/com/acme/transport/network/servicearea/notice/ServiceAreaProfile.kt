package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area profile is in its life cycle. */
enum class ServiceAreaProfileStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A service area profile as the service area team keeps it. */
data class ServiceAreaProfile(
    var code: String,
    var name: String,
    var startsAt: Int,
    var checksum: String,
    var depot: String,
    var dock: String,
    var country: String,
    var cost: Double,
    var timezone: String,
    var status: ServiceAreaProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaProfileStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaProfileStatus): Boolean {
        when (status) {
            ServiceAreaProfileStatus.PROPOSED -> {
                when (next) {
                    ServiceAreaProfileStatus.APPROVED, ServiceAreaProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
            ServiceAreaProfileStatus.APPROVED -> return next == ServiceAreaProfileStatus.REJECTED || next == ServiceAreaProfileStatus.WITHDRAWN
            ServiceAreaProfileStatus.REJECTED -> {
                when (next) {
                    ServiceAreaProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaProfileStatus) {
        if (!canMoveTo(next)) {
            error("service area profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round(startsAt.toDouble() * cost * 100) / 100

    fun snapshot(): ServiceAreaProfile {
        return ServiceAreaProfile(
            code = code,
            name = name,
            startsAt = startsAt,
            checksum = checksum,
            depot = depot,
            dock = dock,
            country = country,
            cost = cost,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a service area profile in status proposed. */
fun newServiceAreaProfile(code: String, name: String): ServiceAreaProfile {
    return ServiceAreaProfile(
        code = code,
        name = name,
        startsAt = 0,
        checksum = "",
        depot = "",
        dock = "",
        country = "",
        cost = 0.0,
        timezone = "",
        status = ServiceAreaProfileStatus.PROPOSED,
    )
}

package com.acme.transport.customs.broker.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional broker variance is in its life cycle. */
enum class RegionalBrokerVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional broker variance. Its code is unique within a tenant. */
data class RegionalBrokerVariance(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var lane: String,
    var limit: Int,
    var routeCode: String,
    var etaMinutes: Int,
    var threshold: Double,
    var odometer: Int,
    var approved: Boolean,
    var status: RegionalBrokerVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalBrokerVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalBrokerVarianceStatus): Boolean {
        when (status) {
            RegionalBrokerVarianceStatus.CREATED -> {
                when (next) {
                    RegionalBrokerVarianceStatus.PUBLISHED, RegionalBrokerVarianceStatus.RETIRED -> return true
                }
                return false
            }
            RegionalBrokerVarianceStatus.VALIDATED -> return next == RegionalBrokerVarianceStatus.PUBLISHED || next == RegionalBrokerVarianceStatus.RETIRED
            RegionalBrokerVarianceStatus.PUBLISHED -> {
                when (next) {
                    RegionalBrokerVarianceStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalBrokerVarianceStatus) {
        if (!canMoveTo(next)) {
            error("regional broker variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedTotal(): Double = round(etaMinutes.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): RegionalBrokerVariance {
        return RegionalBrokerVariance(
            code = code,
            title = title,
            expiresAt = expiresAt,
            lane = lane,
            limit = limit,
            routeCode = routeCode,
            etaMinutes = etaMinutes,
            threshold = threshold,
            odometer = odometer,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a regional broker variance in status created. */
fun newRegionalBrokerVariance(code: String, title: String): RegionalBrokerVariance {
    return RegionalBrokerVariance(
        code = code,
        title = title,
        expiresAt = 0,
        lane = "",
        limit = 0,
        routeCode = "",
        etaMinutes = 0,
        threshold = 0.0,
        odometer = 0,
        approved = false,
        status = RegionalBrokerVarianceStatus.CREATED,
    )
}

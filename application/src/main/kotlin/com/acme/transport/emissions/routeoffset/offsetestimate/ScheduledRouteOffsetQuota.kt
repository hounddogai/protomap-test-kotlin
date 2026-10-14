package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled route offset quota is in its life cycle. */
enum class ScheduledRouteOffsetQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scheduled route offset quota as the route offset team keeps it. */
data class ScheduledRouteOffsetQuota(
    var code: String,
    var title: String,
    var amount: Double,
    var segment: String,
    var shiftCode: String,
    var stopCount: Int,
    var updatedAt: Int,
    var checksum: String,
    var status: ScheduledRouteOffsetQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRouteOffsetQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRouteOffsetQuotaStatus): Boolean {
        when (status) {
            ScheduledRouteOffsetQuotaStatus.IDLE -> {
                when (next) {
                    ScheduledRouteOffsetQuotaStatus.READY, ScheduledRouteOffsetQuotaStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledRouteOffsetQuotaStatus.WARMING -> return next == ScheduledRouteOffsetQuotaStatus.READY
            ScheduledRouteOffsetQuotaStatus.READY -> {
                when (next) {
                    ScheduledRouteOffsetQuotaStatus.DRAINING, ScheduledRouteOffsetQuotaStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledRouteOffsetQuotaStatus.DRAINING -> return next == ScheduledRouteOffsetQuotaStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledRouteOffsetQuotaStatus) {
        if (!canMoveTo(next)) {
            error("scheduled route offset quota $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedLoad(): Double = round(stopCount.toDouble() * amount * 100) / 100

    fun snapshot(): ScheduledRouteOffsetQuota {
        return ScheduledRouteOffsetQuota(
            code = code,
            title = title,
            amount = amount,
            segment = segment,
            shiftCode = shiftCode,
            stopCount = stopCount,
            updatedAt = updatedAt,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled route offset quota in status idle. */
fun newScheduledRouteOffsetQuota(code: String, title: String): ScheduledRouteOffsetQuota {
    return ScheduledRouteOffsetQuota(
        code = code,
        title = title,
        amount = 0.0,
        segment = "",
        shiftCode = "",
        stopCount = 0,
        updatedAt = 0,
        checksum = "",
        status = ScheduledRouteOffsetQuotaStatus.IDLE,
    )
}

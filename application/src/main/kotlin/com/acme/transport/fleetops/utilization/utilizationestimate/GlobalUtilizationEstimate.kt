package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global utilization estimate records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a global utilization estimate is in its life cycle. */
enum class GlobalUtilizationEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global utilization estimate as the utilization team keeps it. */
data class GlobalUtilizationEstimate(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var country: String,
    var depot: String,
    var durationMinutes: Int,
    var odometer: Int,
    var limit: Int,
    var graceDays: Int,
    var status: GlobalUtilizationEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalUtilizationEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalUtilizationEstimateStatus): Boolean {
        when (status) {
            GlobalUtilizationEstimateStatus.PROPOSED -> return next == GlobalUtilizationEstimateStatus.APPROVED || next == GlobalUtilizationEstimateStatus.WITHDRAWN
            GlobalUtilizationEstimateStatus.APPROVED -> return next == GlobalUtilizationEstimateStatus.REJECTED || next == GlobalUtilizationEstimateStatus.WITHDRAWN
            GlobalUtilizationEstimateStatus.REJECTED -> return next == GlobalUtilizationEstimateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalUtilizationEstimateStatus) {
        if (!canMoveTo(next)) {
            error("global utilization estimate $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedVolume(): Double = round((graceDays.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): GlobalUtilizationEstimate {
        return GlobalUtilizationEstimate(
            code = code,
            title = title,
            expiresAt = expiresAt,
            country = country,
            depot = depot,
            durationMinutes = durationMinutes,
            odometer = odometer,
            limit = limit,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global utilization estimate in status proposed. */
fun newGlobalUtilizationEstimate(code: String, title: String): GlobalUtilizationEstimate {
    return GlobalUtilizationEstimate(
        code = code,
        title = title,
        expiresAt = 0,
        country = "",
        depot = "",
        durationMinutes = 0,
        odometer = 0,
        limit = 0,
        graceDays = 0,
        status = GlobalUtilizationEstimateStatus.PROPOSED,
    )
}

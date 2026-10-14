package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped utilization estimate records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a capped utilization estimate is in its life cycle. */
enum class CappedUtilizationEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capped utilization estimate as the utilization team keeps it. */
data class CappedUtilizationEstimate(
    var code: String,
    var label: String,
    var driverId: String,
    var category: String,
    var archived: Boolean,
    var endsAt: Int,
    var limit: Int,
    var contactEmail: String,
    var minimum: Double,
    var windowDays: Int,
    var retries: Int,
    var status: CappedUtilizationEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedUtilizationEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedUtilizationEstimateStatus): Boolean {
        when (status) {
            CappedUtilizationEstimateStatus.OPEN -> {
                when (next) {
                    CappedUtilizationEstimateStatus.IN_REVIEW, CappedUtilizationEstimateStatus.RESOLVED -> return true
                }
                return false
            }
            CappedUtilizationEstimateStatus.IN_REVIEW -> {
                when (next) {
                    CappedUtilizationEstimateStatus.RESOLVED, CappedUtilizationEstimateStatus.REOPENED -> return true
                }
                return false
            }
            CappedUtilizationEstimateStatus.RESOLVED -> return next == CappedUtilizationEstimateStatus.ARCHIVED
            CappedUtilizationEstimateStatus.REOPENED -> {
                when (next) {
                    CappedUtilizationEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedUtilizationEstimateStatus) {
        if (!canMoveTo(next)) {
            error("capped utilization estimate $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"capped utilization estimate"} $code"

    fun adjustedExposure(): Double = round(windowDays.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): CappedUtilizationEstimate {
        return CappedUtilizationEstimate(
            code = code,
            label = label,
            driverId = driverId,
            category = category,
            archived = archived,
            endsAt = endsAt,
            limit = limit,
            contactEmail = contactEmail,
            minimum = minimum,
            windowDays = windowDays,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped utilization estimate in status open. */
fun newCappedUtilizationEstimate(code: String, label: String): CappedUtilizationEstimate {
    return CappedUtilizationEstimate(
        code = code,
        label = label,
        driverId = "",
        category = "",
        archived = false,
        endsAt = 0,
        limit = 0,
        contactEmail = "",
        minimum = 0.0,
        windowDays = 0,
        retries = 0,
        status = CappedUtilizationEstimateStatus.OPEN,
    )
}

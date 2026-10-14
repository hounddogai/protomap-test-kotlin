package com.acme.transport.customs.hold.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual hold estimate is in its life cycle. */
enum class ManualHoldEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One manual hold estimate, with what the planners need to act on it. */
data class ManualHoldEstimate(
    var code: String,
    var title: String,
    var budget: Double,
    var depot: String,
    var createdAt: Int,
    var driverId: String,
    var region: String,
    var status: ManualHoldEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualHoldEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualHoldEstimateStatus): Boolean {
        when (status) {
            ManualHoldEstimateStatus.OPEN -> {
                when (next) {
                    ManualHoldEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualHoldEstimateStatus.IN_REVIEW -> return next == ManualHoldEstimateStatus.ARCHIVED
            ManualHoldEstimateStatus.RESOLVED -> {
                when (next) {
                    ManualHoldEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualHoldEstimateStatus.REOPENED -> return next == ManualHoldEstimateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ManualHoldEstimateStatus) {
        if (!canMoveTo(next)) {
            error("manual hold estimate $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"manual hold estimate"} $code"

    fun effectiveLoad(): Double = round((budget - createdAt.toDouble()) * 100) / 100

    fun snapshot(): ManualHoldEstimate {
        return ManualHoldEstimate(
            code = code,
            title = title,
            budget = budget,
            depot = depot,
            createdAt = createdAt,
            driverId = driverId,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual hold estimate in status open. */
fun newManualHoldEstimate(code: String, title: String): ManualHoldEstimate {
    return ManualHoldEstimate(
        code = code,
        title = title,
        budget = 0.0,
        depot = "",
        createdAt = 0,
        driverId = "",
        region = "",
        status = ManualHoldEstimateStatus.OPEN,
    )
}

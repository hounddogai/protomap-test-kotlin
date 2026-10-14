package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break estimate is in its life cycle. */
enum class RestBreakEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rest break estimate. Its code is unique within a tenant. */
data class RestBreakEstimate(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var currency: String,
    var reviewer: String,
    var depot: String,
    var revision: Int,
    var windowDays: Int,
    var capacity: Int,
    var status: RestBreakEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakEstimateStatus): Boolean {
        when (status) {
            RestBreakEstimateStatus.OPEN -> return next == RestBreakEstimateStatus.RESOLVED
            RestBreakEstimateStatus.IN_REVIEW -> {
                when (next) {
                    RestBreakEstimateStatus.REOPENED -> return true
                }
                return false
            }
            RestBreakEstimateStatus.RESOLVED -> return next == RestBreakEstimateStatus.ARCHIVED
            RestBreakEstimateStatus.REOPENED -> return next == RestBreakEstimateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RestBreakEstimateStatus) {
        if (!canMoveTo(next)) {
            error("rest break estimate $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun effectiveLoad(): Double = round((capacity.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): RestBreakEstimate {
        return RestBreakEstimate(
            code = code,
            name = name,
            distanceKm = distanceKm,
            currency = currency,
            reviewer = reviewer,
            depot = depot,
            revision = revision,
            windowDays = windowDays,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rest break estimate in status open. */
fun newRestBreakEstimate(code: String, name: String): RestBreakEstimate {
    return RestBreakEstimate(
        code = code,
        name = name,
        distanceKm = 0.0,
        currency = "",
        reviewer = "",
        depot = "",
        revision = 0,
        windowDays = 0,
        capacity = 0,
        status = RestBreakEstimateStatus.OPEN,
    )
}

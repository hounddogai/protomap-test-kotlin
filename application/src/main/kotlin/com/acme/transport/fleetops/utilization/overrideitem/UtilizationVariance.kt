package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization variance is in its life cycle. */
enum class UtilizationVarianceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A utilization variance. Its code is unique within a tenant. */
data class UtilizationVariance(
    var code: String,
    var name: String,
    var limit: Int,
    var windowDays: Int,
    var stopCount: Int,
    var status: UtilizationVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationVarianceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationVarianceStatus): Boolean {
        when (status) {
            UtilizationVarianceStatus.DRAFT -> return next == UtilizationVarianceStatus.PENDING || next == UtilizationVarianceStatus.SUSPENDED
            UtilizationVarianceStatus.PENDING -> {
                when (next) {
                    UtilizationVarianceStatus.SUSPENDED -> return true
                }
                return false
            }
            UtilizationVarianceStatus.ACTIVE -> return next == UtilizationVarianceStatus.SUSPENDED || next == UtilizationVarianceStatus.CLOSED
            UtilizationVarianceStatus.SUSPENDED -> {
                when (next) {
                    UtilizationVarianceStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationVarianceStatus) {
        if (!canMoveTo(next)) {
            error("utilization variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"utilization variance"} $code"

    fun netExposure(): Double = round((stopCount.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): UtilizationVariance {
        return UtilizationVariance(
            code = code,
            name = name,
            limit = limit,
            windowDays = windowDays,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a utilization variance in status draft. */
fun newUtilizationVariance(code: String, name: String): UtilizationVariance {
    return UtilizationVariance(
        code = code,
        name = name,
        limit = 0,
        windowDays = 0,
        stopCount = 0,
        status = UtilizationVarianceStatus.DRAFT,
    )
}

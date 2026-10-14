package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time plan is in its life cycle. */
enum class TransitTimePlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A transit time plan. Its code is unique within a tenant. */
data class TransitTimePlan(
    var code: String,
    var title: String,
    var ceiling: Double,
    var windowDays: Int,
    var attempts: Int,
    var status: TransitTimePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimePlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimePlanStatus): Boolean {
        when (status) {
            TransitTimePlanStatus.OPEN -> return next == TransitTimePlanStatus.RESOLVED
            TransitTimePlanStatus.IN_REVIEW -> {
                when (next) {
                    TransitTimePlanStatus.REOPENED, TransitTimePlanStatus.ARCHIVED -> return true
                }
                return false
            }
            TransitTimePlanStatus.RESOLVED -> return next == TransitTimePlanStatus.REOPENED || next == TransitTimePlanStatus.ARCHIVED
            TransitTimePlanStatus.REOPENED -> {
                when (next) {
                    TransitTimePlanStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimePlanStatus) {
        if (!canMoveTo(next)) {
            error("transit time plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"transit time plan"} $code"

    fun projectedLoad(): Double = round((ceiling + attempts.toDouble()) * 100) / 100

    fun snapshot(): TransitTimePlan {
        return TransitTimePlan(
            code = code,
            title = title,
            ceiling = ceiling,
            windowDays = windowDays,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a transit time plan in status open. */
fun newTransitTimePlan(code: String, title: String): TransitTimePlan {
    return TransitTimePlan(
        code = code,
        title = title,
        ceiling = 0.0,
        windowDays = 0,
        attempts = 0,
        status = TransitTimePlanStatus.OPEN,
    )
}

package com.acme.transport.safety.coaching.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped coaching plan is in its life cycle. */
enum class CappedCoachingPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One capped coaching plan, with what the planners need to act on it. */
data class CappedCoachingPlan(
    var code: String,
    var title: String,
    var createdAt: Int,
    var batchSize: Int,
    var routeCode: String,
    var phone: String,
    var country: String,
    var attempts: Int,
    var depot: String,
    var retries: Int,
    var status: CappedCoachingPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCoachingPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCoachingPlanStatus): Boolean {
        when (status) {
            CappedCoachingPlanStatus.DRAFT -> return next == CappedCoachingPlanStatus.PENDING || next == CappedCoachingPlanStatus.CLOSED
            CappedCoachingPlanStatus.PENDING -> return next == CappedCoachingPlanStatus.CLOSED
            CappedCoachingPlanStatus.ACTIVE -> return next == CappedCoachingPlanStatus.SUSPENDED || next == CappedCoachingPlanStatus.CLOSED
            CappedCoachingPlanStatus.SUSPENDED -> {
                when (next) {
                    CappedCoachingPlanStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCoachingPlanStatus) {
        if (!canMoveTo(next)) {
            error("capped coaching plan $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedVolume(): Double = round((retries.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): CappedCoachingPlan {
        return CappedCoachingPlan(
            code = code,
            title = title,
            createdAt = createdAt,
            batchSize = batchSize,
            routeCode = routeCode,
            phone = phone,
            country = country,
            attempts = attempts,
            depot = depot,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped coaching plan in status draft. */
fun newCappedCoachingPlan(code: String, title: String): CappedCoachingPlan {
    return CappedCoachingPlan(
        code = code,
        title = title,
        createdAt = 0,
        batchSize = 0,
        routeCode = "",
        phone = "",
        country = "",
        attempts = 0,
        depot = "",
        retries = 0,
        status = CappedCoachingPlanStatus.DRAFT,
    )
}

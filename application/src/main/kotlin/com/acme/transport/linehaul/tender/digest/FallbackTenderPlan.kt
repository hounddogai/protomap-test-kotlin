package com.acme.transport.linehaul.tender.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback tender plan is in its life cycle. */
enum class FallbackTenderPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a fallback tender plan and its current status. */
data class FallbackTenderPlan(
    var code: String,
    var name: String,
    var stopCount: Int,
    var startsAt: Int,
    var capacity: Int,
    var ownerId: String,
    var status: FallbackTenderPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTenderPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTenderPlanStatus): Boolean {
        when (status) {
            FallbackTenderPlanStatus.OPEN -> return next == FallbackTenderPlanStatus.IN_REVIEW || next == FallbackTenderPlanStatus.ARCHIVED
            FallbackTenderPlanStatus.IN_REVIEW -> {
                when (next) {
                    FallbackTenderPlanStatus.REOPENED -> return true
                }
                return false
            }
            FallbackTenderPlanStatus.RESOLVED -> return next == FallbackTenderPlanStatus.REOPENED
            FallbackTenderPlanStatus.REOPENED -> {
                when (next) {
                    FallbackTenderPlanStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackTenderPlanStatus) {
        if (!canMoveTo(next)) {
            error("fallback tender plan $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round((capacity.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): FallbackTenderPlan {
        return FallbackTenderPlan(
            code = code,
            name = name,
            stopCount = stopCount,
            startsAt = startsAt,
            capacity = capacity,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback tender plan in status open. */
fun newFallbackTenderPlan(code: String, name: String): FallbackTenderPlan {
    return FallbackTenderPlan(
        code = code,
        name = name,
        stopCount = 0,
        startsAt = 0,
        capacity = 0,
        ownerId = "",
        status = FallbackTenderPlanStatus.OPEN,
    )
}

package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback locker plan is in its life cycle. */
enum class FallbackLockerPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback locker plan as the locker team keeps it. */
data class FallbackLockerPlan(
    var code: String,
    var title: String,
    var threshold: Double,
    var maximum: Double,
    var fuelLiters: Double,
    var status: FallbackLockerPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackLockerPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackLockerPlanStatus): Boolean {
        when (status) {
            FallbackLockerPlanStatus.CREATED -> return next == FallbackLockerPlanStatus.VALIDATED || next == FallbackLockerPlanStatus.PUBLISHED
            FallbackLockerPlanStatus.VALIDATED -> return next == FallbackLockerPlanStatus.PUBLISHED || next == FallbackLockerPlanStatus.RETIRED
            FallbackLockerPlanStatus.PUBLISHED -> return next == FallbackLockerPlanStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackLockerPlanStatus) {
        if (!canMoveTo(next)) {
            error("fallback locker plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round(threshold * maximum * 100) / 100

    fun snapshot(): FallbackLockerPlan {
        return FallbackLockerPlan(
            code = code,
            title = title,
            threshold = threshold,
            maximum = maximum,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback locker plan in status created. */
fun newFallbackLockerPlan(code: String, title: String): FallbackLockerPlan {
    return FallbackLockerPlan(
        code = code,
        title = title,
        threshold = 0.0,
        maximum = 0.0,
        fuelLiters = 0.0,
        status = FallbackLockerPlanStatus.CREATED,
    )
}

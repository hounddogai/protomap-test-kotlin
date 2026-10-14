package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict locker plan is in its life cycle. */
enum class StrictLockerPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict locker plan. Its code is unique within a tenant. */
data class StrictLockerPlan(
    var code: String,
    var label: String,
    var segment: String,
    var loadKg: Double,
    var discount: Double,
    var title: String,
    var graceDays: Int,
    var threshold: Double,
    var status: StrictLockerPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictLockerPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictLockerPlanStatus): Boolean {
        when (status) {
            StrictLockerPlanStatus.OPEN -> return next == StrictLockerPlanStatus.RESOLVED || next == StrictLockerPlanStatus.REOPENED
            StrictLockerPlanStatus.IN_REVIEW -> {
                when (next) {
                    StrictLockerPlanStatus.RESOLVED -> return true
                }
                return false
            }
            StrictLockerPlanStatus.RESOLVED -> return next == StrictLockerPlanStatus.ARCHIVED
            StrictLockerPlanStatus.REOPENED -> {
                when (next) {
                    StrictLockerPlanStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictLockerPlanStatus) {
        if (!canMoveTo(next)) {
            error("strict locker plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveVolume(): Double = round((loadKg - graceDays.toDouble()) * 100) / 100

    fun snapshot(): StrictLockerPlan {
        return StrictLockerPlan(
            code = code,
            label = label,
            segment = segment,
            loadKg = loadKg,
            discount = discount,
            title = title,
            graceDays = graceDays,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a strict locker plan in status open. */
fun newStrictLockerPlan(code: String, label: String): StrictLockerPlan {
    return StrictLockerPlan(
        code = code,
        label = label,
        segment = "",
        loadKg = 0.0,
        discount = 0.0,
        title = "",
        graceDays = 0,
        threshold = 0.0,
        status = StrictLockerPlanStatus.OPEN,
    )
}

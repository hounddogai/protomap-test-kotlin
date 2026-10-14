package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive defect plan is in its life cycle. */
enum class AdaptiveDefectPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One adaptive defect plan, with what the planners need to act on it. */
data class AdaptiveDefectPlan(
    var code: String,
    var name: String,
    var batchSize: Int,
    var version: Int,
    var pallets: Int,
    var minimum: Double,
    var endsAt: Int,
    var status: AdaptiveDefectPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDefectPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDefectPlanStatus): Boolean {
        when (status) {
            AdaptiveDefectPlanStatus.DRAFT -> {
                when (next) {
                    AdaptiveDefectPlanStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveDefectPlanStatus.PENDING -> return next == AdaptiveDefectPlanStatus.ACTIVE
            AdaptiveDefectPlanStatus.ACTIVE -> return next == AdaptiveDefectPlanStatus.SUSPENDED
            AdaptiveDefectPlanStatus.SUSPENDED -> return next == AdaptiveDefectPlanStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveDefectPlanStatus) {
        if (!canMoveTo(next)) {
            error("adaptive defect plan $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code / $name"

    fun adjustedTotal(): Double = round((endsAt.toDouble() + minimum) * 100) / 100

    fun snapshot(): AdaptiveDefectPlan {
        return AdaptiveDefectPlan(
            code = code,
            name = name,
            batchSize = batchSize,
            version = version,
            pallets = pallets,
            minimum = minimum,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive defect plan in status draft. */
fun newAdaptiveDefectPlan(code: String, name: String): AdaptiveDefectPlan {
    return AdaptiveDefectPlan(
        code = code,
        name = name,
        batchSize = 0,
        version = 0,
        pallets = 0,
        minimum = 0.0,
        endsAt = 0,
        status = AdaptiveDefectPlanStatus.DRAFT,
    )
}

package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.text.normalize

/** Where a weighted lane plan target is in its life cycle. */
enum class WeightedLanePlanTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted lane plan target as the lane plan team keeps it. */
data class WeightedLanePlanTarget(
    var code: String,
    var name: String,
    var graceDays: Int,
    var currency: String,
    var description: String,
    var status: WeightedLanePlanTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedLanePlanTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedLanePlanTargetStatus): Boolean {
        when (status) {
            WeightedLanePlanTargetStatus.OPEN -> {
                when (next) {
                    WeightedLanePlanTargetStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedLanePlanTargetStatus.IN_REVIEW -> return next == WeightedLanePlanTargetStatus.REOPENED || next == WeightedLanePlanTargetStatus.ARCHIVED
            WeightedLanePlanTargetStatus.RESOLVED -> return next == WeightedLanePlanTargetStatus.ARCHIVED
            WeightedLanePlanTargetStatus.REOPENED -> return next == WeightedLanePlanTargetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: WeightedLanePlanTargetStatus) {
        if (!canMoveTo(next)) {
            error("weighted lane plan target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): WeightedLanePlanTarget {
        return WeightedLanePlanTarget(
            code = code,
            name = name,
            graceDays = graceDays,
            currency = currency,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted lane plan target in status open. */
fun newWeightedLanePlanTarget(code: String, name: String): WeightedLanePlanTarget {
    return WeightedLanePlanTarget(
        code = code,
        name = name,
        graceDays = 0,
        currency = "",
        description = "",
        status = WeightedLanePlanTargetStatus.OPEN,
    )
}

package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.text.normalize

/** Where a weighted service plan target is in its life cycle. */
enum class WeightedServicePlanTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted service plan target as the service plan team keeps it. */
data class WeightedServicePlanTarget(
    var code: String,
    var label: String,
    var carrier: String,
    var priority: Int,
    var description: String,
    var shiftCode: String,
    var status: WeightedServicePlanTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedServicePlanTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedServicePlanTargetStatus): Boolean {
        when (status) {
            WeightedServicePlanTargetStatus.OPEN -> return next == WeightedServicePlanTargetStatus.RESOLVED || next == WeightedServicePlanTargetStatus.ARCHIVED
            WeightedServicePlanTargetStatus.IN_REVIEW -> return next == WeightedServicePlanTargetStatus.REOPENED || next == WeightedServicePlanTargetStatus.ARCHIVED
            WeightedServicePlanTargetStatus.RESOLVED -> {
                when (next) {
                    WeightedServicePlanTargetStatus.REOPENED, WeightedServicePlanTargetStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedServicePlanTargetStatus.REOPENED -> {
                when (next) {
                    WeightedServicePlanTargetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedServicePlanTargetStatus) {
        if (!canMoveTo(next)) {
            error("weighted service plan target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): WeightedServicePlanTarget {
        return WeightedServicePlanTarget(
            code = code,
            label = label,
            carrier = carrier,
            priority = priority,
            description = description,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted service plan target in status open. */
fun newWeightedServicePlanTarget(code: String, label: String): WeightedServicePlanTarget {
    return WeightedServicePlanTarget(
        code = code,
        label = label,
        carrier = "",
        priority = 0,
        description = "",
        shiftCode = "",
        status = WeightedServicePlanTargetStatus.OPEN,
    )
}

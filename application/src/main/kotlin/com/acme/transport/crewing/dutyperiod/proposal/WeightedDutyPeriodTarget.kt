package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted duty period target is in its life cycle. */
enum class WeightedDutyPeriodTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One weighted duty period target, with what the planners need to act on it. */
data class WeightedDutyPeriodTarget(
    var code: String,
    var name: String,
    var phone: String,
    var contactEmail: String,
    var loadKg: Double,
    var floorValue: Double,
    var status: WeightedDutyPeriodTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDutyPeriodTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDutyPeriodTargetStatus): Boolean {
        when (status) {
            WeightedDutyPeriodTargetStatus.PLANNED -> return next == WeightedDutyPeriodTargetStatus.SCHEDULED || next == WeightedDutyPeriodTargetStatus.COMPLETED
            WeightedDutyPeriodTargetStatus.SCHEDULED -> {
                when (next) {
                    WeightedDutyPeriodTargetStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedDutyPeriodTargetStatus.IN_PROGRESS -> return next == WeightedDutyPeriodTargetStatus.COMPLETED || next == WeightedDutyPeriodTargetStatus.CANCELLED
            WeightedDutyPeriodTargetStatus.COMPLETED -> {
                when (next) {
                    WeightedDutyPeriodTargetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDutyPeriodTargetStatus) {
        if (!canMoveTo(next)) {
            error("weighted duty period target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netExposure(): Double = round((floorValue - loadKg) * 100) / 100

    fun snapshot(): WeightedDutyPeriodTarget {
        return WeightedDutyPeriodTarget(
            code = code,
            name = name,
            phone = phone,
            contactEmail = contactEmail,
            loadKg = loadKg,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted duty period target in status planned. */
fun newWeightedDutyPeriodTarget(code: String, name: String): WeightedDutyPeriodTarget {
    return WeightedDutyPeriodTarget(
        code = code,
        name = name,
        phone = "",
        contactEmail = "",
        loadKg = 0.0,
        floorValue = 0.0,
        status = WeightedDutyPeriodTargetStatus.PLANNED,
    )
}

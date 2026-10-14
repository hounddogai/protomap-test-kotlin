package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted duty period threshold is in its life cycle. */
enum class WeightedDutyPeriodThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted duty period threshold. Its code is unique within a tenant. */
data class WeightedDutyPeriodThreshold(
    var code: String,
    var label: String,
    var carrier: String,
    var fuelLiters: Double,
    var margin: Double,
    var startsAt: Int,
    var category: String,
    var name: String,
    var vehicleId: String,
    var status: WeightedDutyPeriodThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDutyPeriodThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDutyPeriodThresholdStatus): Boolean {
        when (status) {
            WeightedDutyPeriodThresholdStatus.PROPOSED -> {
                when (next) {
                    WeightedDutyPeriodThresholdStatus.APPROVED, WeightedDutyPeriodThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedDutyPeriodThresholdStatus.APPROVED -> {
                when (next) {
                    WeightedDutyPeriodThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedDutyPeriodThresholdStatus.REJECTED -> {
                when (next) {
                    WeightedDutyPeriodThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDutyPeriodThresholdStatus) {
        if (!canMoveTo(next)) {
            error("weighted duty period threshold $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted duty period threshold"} $code"

    fun weightedVolume(): Double = round(fuelLiters * margin * 100) / 100

    fun snapshot(): WeightedDutyPeriodThreshold {
        return WeightedDutyPeriodThreshold(
            code = code,
            label = label,
            carrier = carrier,
            fuelLiters = fuelLiters,
            margin = margin,
            startsAt = startsAt,
            category = category,
            name = name,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted duty period threshold in status proposed. */
fun newWeightedDutyPeriodThreshold(code: String, label: String): WeightedDutyPeriodThreshold {
    return WeightedDutyPeriodThreshold(
        code = code,
        label = label,
        carrier = "",
        fuelLiters = 0.0,
        margin = 0.0,
        startsAt = 0,
        category = "",
        name = "",
        vehicleId = "",
        status = WeightedDutyPeriodThresholdStatus.PROPOSED,
    )
}

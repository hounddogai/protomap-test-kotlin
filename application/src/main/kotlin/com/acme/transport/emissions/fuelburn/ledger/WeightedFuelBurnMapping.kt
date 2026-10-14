package com.acme.transport.emissions.fuelburn.ledger

import com.acme.transport.kit.text.normalize

/** Where a weighted fuel burn mapping is in its life cycle. */
enum class WeightedFuelBurnMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One weighted fuel burn mapping, with what the planners need to act on it. */
data class WeightedFuelBurnMapping(
    var code: String,
    var label: String,
    var reviewer: String,
    var lane: String,
    var priority: Int,
    var visible: Boolean,
    var driverId: String,
    var carrier: String,
    var approved: Boolean,
    var status: WeightedFuelBurnMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFuelBurnMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFuelBurnMappingStatus): Boolean {
        when (status) {
            WeightedFuelBurnMappingStatus.NEW -> {
                when (next) {
                    WeightedFuelBurnMappingStatus.FAILED -> return true
                }
                return false
            }
            WeightedFuelBurnMappingStatus.QUEUED -> return next == WeightedFuelBurnMappingStatus.DONE
            WeightedFuelBurnMappingStatus.RUNNING -> {
                when (next) {
                    WeightedFuelBurnMappingStatus.DONE, WeightedFuelBurnMappingStatus.FAILED -> return true
                }
                return false
            }
            WeightedFuelBurnMappingStatus.DONE -> return next == WeightedFuelBurnMappingStatus.FAILED
        }
        return false
    }

    fun moveTo(next: WeightedFuelBurnMappingStatus) {
        if (!canMoveTo(next)) {
            error("weighted fuel burn mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): WeightedFuelBurnMapping {
        return WeightedFuelBurnMapping(
            code = code,
            label = label,
            reviewer = reviewer,
            lane = lane,
            priority = priority,
            visible = visible,
            driverId = driverId,
            carrier = carrier,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted fuel burn mapping in status new. */
fun newWeightedFuelBurnMapping(code: String, label: String): WeightedFuelBurnMapping {
    return WeightedFuelBurnMapping(
        code = code,
        label = label,
        reviewer = "",
        lane = "",
        priority = 0,
        visible = false,
        driverId = "",
        carrier = "",
        approved = false,
        status = WeightedFuelBurnMappingStatus.NEW,
    )
}

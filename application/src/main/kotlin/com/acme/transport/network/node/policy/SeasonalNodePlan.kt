package com.acme.transport.network.node.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal node plan is in its life cycle. */
enum class SeasonalNodePlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One seasonal node plan, with what the planners need to act on it. */
data class SeasonalNodePlan(
    var code: String,
    var label: String,
    var floorValue: Double,
    var category: String,
    var vehicleId: String,
    var source: String,
    var description: String,
    var fuelLiters: Double,
    var status: SeasonalNodePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalNodePlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalNodePlanStatus): Boolean {
        when (status) {
            SeasonalNodePlanStatus.NEW -> return next == SeasonalNodePlanStatus.QUEUED || next == SeasonalNodePlanStatus.DONE
            SeasonalNodePlanStatus.QUEUED -> {
                when (next) {
                    SeasonalNodePlanStatus.RUNNING, SeasonalNodePlanStatus.FAILED -> return true
                }
                return false
            }
            SeasonalNodePlanStatus.RUNNING -> return next == SeasonalNodePlanStatus.DONE || next == SeasonalNodePlanStatus.FAILED
            SeasonalNodePlanStatus.DONE -> return next == SeasonalNodePlanStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SeasonalNodePlanStatus) {
        if (!canMoveTo(next)) {
            error("seasonal node plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netVolume(): Double = round(floorValue * fuelLiters * 100) / 100

    fun snapshot(): SeasonalNodePlan {
        return SeasonalNodePlan(
            code = code,
            label = label,
            floorValue = floorValue,
            category = category,
            vehicleId = vehicleId,
            source = source,
            description = description,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal node plan in status new. */
fun newSeasonalNodePlan(code: String, label: String): SeasonalNodePlan {
    return SeasonalNodePlan(
        code = code,
        label = label,
        floorValue = 0.0,
        category = "",
        vehicleId = "",
        source = "",
        description = "",
        fuelLiters = 0.0,
        status = SeasonalNodePlanStatus.NEW,
    )
}

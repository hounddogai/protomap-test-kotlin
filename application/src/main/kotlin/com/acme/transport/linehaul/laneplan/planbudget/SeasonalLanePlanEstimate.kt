package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal lane plan estimate is in its life cycle. */
enum class SeasonalLanePlanEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal lane plan estimate as the lane plan team keeps it. */
data class SeasonalLanePlanEstimate(
    var code: String,
    var name: String,
    var phone: String,
    var graceDays: Int,
    var pallets: Int,
    var maximum: Double,
    var vehicleId: String,
    var status: SeasonalLanePlanEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalLanePlanEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalLanePlanEstimateStatus): Boolean {
        when (status) {
            SeasonalLanePlanEstimateStatus.NEW -> return next == SeasonalLanePlanEstimateStatus.RUNNING
            SeasonalLanePlanEstimateStatus.QUEUED -> {
                when (next) {
                    SeasonalLanePlanEstimateStatus.FAILED -> return true
                }
                return false
            }
            SeasonalLanePlanEstimateStatus.RUNNING -> {
                when (next) {
                    SeasonalLanePlanEstimateStatus.DONE -> return true
                }
                return false
            }
            SeasonalLanePlanEstimateStatus.DONE -> {
                when (next) {
                    SeasonalLanePlanEstimateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalLanePlanEstimateStatus) {
        if (!canMoveTo(next)) {
            error("seasonal lane plan estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round(pallets.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): SeasonalLanePlanEstimate {
        return SeasonalLanePlanEstimate(
            code = code,
            name = name,
            phone = phone,
            graceDays = graceDays,
            pallets = pallets,
            maximum = maximum,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal lane plan estimate in status new. */
fun newSeasonalLanePlanEstimate(code: String, name: String): SeasonalLanePlanEstimate {
    return SeasonalLanePlanEstimate(
        code = code,
        name = name,
        phone = "",
        graceDays = 0,
        pallets = 0,
        maximum = 0.0,
        vehicleId = "",
        status = SeasonalLanePlanEstimateStatus.NEW,
    )
}

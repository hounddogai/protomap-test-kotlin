package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global fuel burn estimate is in its life cycle. */
enum class GlobalFuelBurnEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One global fuel burn estimate, with what the planners need to act on it. */
data class GlobalFuelBurnEstimate(
    var code: String,
    var label: String,
    var archived: Boolean,
    var ceiling: Double,
    var loadKg: Double,
    var limit: Int,
    var status: GlobalFuelBurnEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalFuelBurnEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalFuelBurnEstimateStatus): Boolean {
        when (status) {
            GlobalFuelBurnEstimateStatus.REQUESTED -> return next == GlobalFuelBurnEstimateStatus.EXPIRED
            GlobalFuelBurnEstimateStatus.GRANTED -> {
                when (next) {
                    GlobalFuelBurnEstimateStatus.REVOKED, GlobalFuelBurnEstimateStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalFuelBurnEstimateStatus.REVOKED -> return next == GlobalFuelBurnEstimateStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalFuelBurnEstimateStatus) {
        if (!canMoveTo(next)) {
            error("global fuel burn estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round((limit.toDouble() + loadKg) * 100) / 100

    fun snapshot(): GlobalFuelBurnEstimate {
        return GlobalFuelBurnEstimate(
            code = code,
            label = label,
            archived = archived,
            ceiling = ceiling,
            loadKg = loadKg,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global fuel burn estimate in status requested. */
fun newGlobalFuelBurnEstimate(code: String, label: String): GlobalFuelBurnEstimate {
    return GlobalFuelBurnEstimate(
        code = code,
        label = label,
        archived = false,
        ceiling = 0.0,
        loadKg = 0.0,
        limit = 0,
        status = GlobalFuelBurnEstimateStatus.REQUESTED,
    )
}

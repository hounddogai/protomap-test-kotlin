package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal fuel surcharge tier is in its life cycle. */
enum class SeasonalFuelSurchargeTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a seasonal fuel surcharge tier and its current status. */
data class SeasonalFuelSurchargeTier(
    var code: String,
    var label: String,
    var ratio: Double,
    var etaMinutes: Int,
    var active: Boolean,
    var floorValue: Double,
    var status: SeasonalFuelSurchargeTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalFuelSurchargeTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalFuelSurchargeTierStatus): Boolean {
        when (status) {
            SeasonalFuelSurchargeTierStatus.IDLE -> return next == SeasonalFuelSurchargeTierStatus.STOPPED
            SeasonalFuelSurchargeTierStatus.WARMING -> return next == SeasonalFuelSurchargeTierStatus.DRAINING
            SeasonalFuelSurchargeTierStatus.READY -> {
                when (next) {
                    SeasonalFuelSurchargeTierStatus.DRAINING, SeasonalFuelSurchargeTierStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalFuelSurchargeTierStatus.DRAINING -> {
                when (next) {
                    SeasonalFuelSurchargeTierStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalFuelSurchargeTierStatus) {
        if (!canMoveTo(next)) {
            error("seasonal fuel surcharge tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedLoad(): Double = round(ratio * floorValue * 100) / 100

    fun snapshot(): SeasonalFuelSurchargeTier {
        return SeasonalFuelSurchargeTier(
            code = code,
            label = label,
            ratio = ratio,
            etaMinutes = etaMinutes,
            active = active,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal fuel surcharge tier in status idle. */
fun newSeasonalFuelSurchargeTier(code: String, label: String): SeasonalFuelSurchargeTier {
    return SeasonalFuelSurchargeTier(
        code = code,
        label = label,
        ratio = 0.0,
        etaMinutes = 0,
        active = false,
        floorValue = 0.0,
        status = SeasonalFuelSurchargeTierStatus.IDLE,
    )
}

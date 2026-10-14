package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn score is in its life cycle. */
enum class FuelBurnScoreStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fuel burn score, with what the planners need to act on it. */
data class FuelBurnScore(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var rate: Double,
    var createdAt: Int,
    var budget: Double,
    var windowDays: Int,
    var threshold: Double,
    var status: FuelBurnScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnScoreStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnScoreStatus): Boolean {
        when (status) {
            FuelBurnScoreStatus.IDLE -> return next == FuelBurnScoreStatus.WARMING || next == FuelBurnScoreStatus.READY
            FuelBurnScoreStatus.WARMING -> {
                when (next) {
                    FuelBurnScoreStatus.READY, FuelBurnScoreStatus.STOPPED -> return true
                }
                return false
            }
            FuelBurnScoreStatus.READY -> return next == FuelBurnScoreStatus.STOPPED
            FuelBurnScoreStatus.DRAINING -> {
                when (next) {
                    FuelBurnScoreStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnScoreStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn score $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveVolume(): Double = round((rate - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnScore {
        return FuelBurnScore(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            rate = rate,
            createdAt = createdAt,
            budget = budget,
            windowDays = windowDays,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fuel burn score in status idle. */
fun newFuelBurnScore(code: String, label: String): FuelBurnScore {
    return FuelBurnScore(
        code = code,
        label = label,
        etaMinutes = 0,
        rate = 0.0,
        createdAt = 0,
        budget = 0.0,
        windowDays = 0,
        threshold = 0.0,
        status = FuelBurnScoreStatus.IDLE,
    )
}

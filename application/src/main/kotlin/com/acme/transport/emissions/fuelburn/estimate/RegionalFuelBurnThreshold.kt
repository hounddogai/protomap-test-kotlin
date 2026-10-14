package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional fuel burn threshold is in its life cycle. */
enum class RegionalFuelBurnThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a regional fuel burn threshold and its current status. */
data class RegionalFuelBurnThreshold(
    var code: String,
    var label: String,
    var capacity: Int,
    var loadKg: Double,
    var channel: String,
    var status: RegionalFuelBurnThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelBurnThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelBurnThresholdStatus): Boolean {
        when (status) {
            RegionalFuelBurnThresholdStatus.PLANNED -> {
                when (next) {
                    RegionalFuelBurnThresholdStatus.COMPLETED, RegionalFuelBurnThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalFuelBurnThresholdStatus.SCHEDULED -> {
                when (next) {
                    RegionalFuelBurnThresholdStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RegionalFuelBurnThresholdStatus.IN_PROGRESS -> return next == RegionalFuelBurnThresholdStatus.COMPLETED
            RegionalFuelBurnThresholdStatus.COMPLETED -> {
                when (next) {
                    RegionalFuelBurnThresholdStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalFuelBurnThresholdStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel burn threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional fuel burn threshold"} $code"

    fun projectedVolume(): Double = round((capacity.toDouble() + loadKg) * 100) / 100

    fun snapshot(): RegionalFuelBurnThreshold {
        return RegionalFuelBurnThreshold(
            code = code,
            label = label,
            capacity = capacity,
            loadKg = loadKg,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional fuel burn threshold in status planned. */
fun newRegionalFuelBurnThreshold(code: String, label: String): RegionalFuelBurnThreshold {
    return RegionalFuelBurnThreshold(
        code = code,
        label = label,
        capacity = 0,
        loadKg = 0.0,
        channel = "",
        status = RegionalFuelBurnThresholdStatus.PLANNED,
    )
}

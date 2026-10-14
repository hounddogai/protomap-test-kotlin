package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled fuel burn threshold records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a scheduled fuel burn threshold is in its life cycle. */
enum class ScheduledFuelBurnThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a scheduled fuel burn threshold and its current status. */
data class ScheduledFuelBurnThreshold(
    var code: String,
    var name: String,
    var active: Boolean,
    var createdAt: Int,
    var reviewer: String,
    var amount: Double,
    var status: ScheduledFuelBurnThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledFuelBurnThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledFuelBurnThresholdStatus): Boolean {
        when (status) {
            ScheduledFuelBurnThresholdStatus.REQUESTED -> {
                when (next) {
                    ScheduledFuelBurnThresholdStatus.REVOKED -> return true
                }
                return false
            }
            ScheduledFuelBurnThresholdStatus.GRANTED -> return next == ScheduledFuelBurnThresholdStatus.REVOKED || next == ScheduledFuelBurnThresholdStatus.EXPIRED
            ScheduledFuelBurnThresholdStatus.REVOKED -> {
                when (next) {
                    ScheduledFuelBurnThresholdStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledFuelBurnThresholdStatus) {
        if (!canMoveTo(next)) {
            error("scheduled fuel burn threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled fuel burn threshold"} $code"

    fun effectiveVolume(): Double = round((amount + createdAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledFuelBurnThreshold {
        return ScheduledFuelBurnThreshold(
            code = code,
            name = name,
            active = active,
            createdAt = createdAt,
            reviewer = reviewer,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled fuel burn threshold in status requested. */
fun newScheduledFuelBurnThreshold(code: String, name: String): ScheduledFuelBurnThreshold {
    return ScheduledFuelBurnThreshold(
        code = code,
        name = name,
        active = false,
        createdAt = 0,
        reviewer = "",
        amount = 0.0,
        status = ScheduledFuelBurnThresholdStatus.REQUESTED,
    )
}

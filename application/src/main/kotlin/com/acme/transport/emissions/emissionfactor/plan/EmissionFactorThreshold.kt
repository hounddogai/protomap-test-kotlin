package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor threshold is in its life cycle. */
enum class EmissionFactorThresholdStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One emission factor threshold, with what the planners need to act on it. */
data class EmissionFactorThreshold(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var reviewer: String,
    var priority: Int,
    var fuelLiters: Double,
    var driverId: String,
    var status: EmissionFactorThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorThresholdStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorThresholdStatus): Boolean {
        when (status) {
            EmissionFactorThresholdStatus.DRAFT -> {
                when (next) {
                    EmissionFactorThresholdStatus.SUSPENDED -> return true
                }
                return false
            }
            EmissionFactorThresholdStatus.PENDING -> return next == EmissionFactorThresholdStatus.ACTIVE || next == EmissionFactorThresholdStatus.SUSPENDED
            EmissionFactorThresholdStatus.ACTIVE -> {
                when (next) {
                    EmissionFactorThresholdStatus.SUSPENDED, EmissionFactorThresholdStatus.CLOSED -> return true
                }
                return false
            }
            EmissionFactorThresholdStatus.SUSPENDED -> {
                when (next) {
                    EmissionFactorThresholdStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorThresholdStatus) {
        if (!canMoveTo(next)) {
            error("emission factor threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"emission factor threshold"} $code"

    fun effectiveLoad(): Double = round(fuelLiters * priority.toDouble() * 100) / 100

    fun snapshot(): EmissionFactorThreshold {
        return EmissionFactorThreshold(
            code = code,
            label = label,
            pinned = pinned,
            reviewer = reviewer,
            priority = priority,
            fuelLiters = fuelLiters,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a emission factor threshold in status draft. */
fun newEmissionFactorThreshold(code: String, label: String): EmissionFactorThreshold {
    return EmissionFactorThreshold(
        code = code,
        label = label,
        pinned = false,
        reviewer = "",
        priority = 0,
        fuelLiters = 0.0,
        driverId = "",
        status = EmissionFactorThresholdStatus.DRAFT,
    )
}

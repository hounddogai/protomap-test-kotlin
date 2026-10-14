package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The ev charge baseline records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a ev charge baseline is in its life cycle. */
enum class EvChargeBaselineStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One ev charge baseline, with what the planners need to act on it. */
data class EvChargeBaseline(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var notes: String,
    var vehicleId: String,
    var amount: Double,
    var status: EvChargeBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeBaselineStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeBaselineStatus): Boolean {
        when (status) {
            EvChargeBaselineStatus.CREATED -> {
                when (next) {
                    EvChargeBaselineStatus.PUBLISHED -> return true
                }
                return false
            }
            EvChargeBaselineStatus.VALIDATED -> return next == EvChargeBaselineStatus.PUBLISHED || next == EvChargeBaselineStatus.RETIRED
            EvChargeBaselineStatus.PUBLISHED -> {
                when (next) {
                    EvChargeBaselineStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeBaselineStatus) {
        if (!canMoveTo(next)) {
            error("ev charge baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"ev charge baseline"} $code"

    fun adjustedLoad(): Double = round((fuelLiters + amount) * 100) / 100

    fun snapshot(): EvChargeBaseline {
        return EvChargeBaseline(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            notes = notes,
            vehicleId = vehicleId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a ev charge baseline in status created. */
fun newEvChargeBaseline(code: String, label: String): EvChargeBaseline {
    return EvChargeBaseline(
        code = code,
        label = label,
        fuelLiters = 0.0,
        notes = "",
        vehicleId = "",
        amount = 0.0,
        status = EvChargeBaselineStatus.CREATED,
    )
}

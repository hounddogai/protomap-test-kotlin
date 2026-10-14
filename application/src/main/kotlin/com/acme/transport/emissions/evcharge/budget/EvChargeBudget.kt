package com.acme.transport.emissions.evcharge.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The ev charge budget records of emissions's ev charge work, and the rules, stores, and calculations around
// them.

/** Where a ev charge budget is in its life cycle. */
enum class EvChargeBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One ev charge budget, with what the planners need to act on it. */
data class EvChargeBudget(
    var code: String,
    var label: String,
    var floorValue: Double,
    var durationMinutes: Int,
    var trailerId: String,
    var approved: Boolean,
    var vehicleId: String,
    var notes: String,
    var region: String,
    var graceDays: Int,
    var endsAt: Int,
    var status: EvChargeBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeBudgetStatus): Boolean {
        when (status) {
            EvChargeBudgetStatus.IDLE -> return next == EvChargeBudgetStatus.DRAINING
            EvChargeBudgetStatus.WARMING -> return next == EvChargeBudgetStatus.STOPPED
            EvChargeBudgetStatus.READY -> {
                when (next) {
                    EvChargeBudgetStatus.DRAINING -> return true
                }
                return false
            }
            EvChargeBudgetStatus.DRAINING -> return next == EvChargeBudgetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: EvChargeBudgetStatus) {
        if (!canMoveTo(next)) {
            error("ev charge budget $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun netTotal(): Double = round((endsAt.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): EvChargeBudget {
        return EvChargeBudget(
            code = code,
            label = label,
            floorValue = floorValue,
            durationMinutes = durationMinutes,
            trailerId = trailerId,
            approved = approved,
            vehicleId = vehicleId,
            notes = notes,
            region = region,
            graceDays = graceDays,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a ev charge budget in status idle. */
fun newEvChargeBudget(code: String, label: String): EvChargeBudget {
    return EvChargeBudget(
        code = code,
        label = label,
        floorValue = 0.0,
        durationMinutes = 0,
        trailerId = "",
        approved = false,
        vehicleId = "",
        notes = "",
        region = "",
        graceDays = 0,
        endsAt = 0,
        status = EvChargeBudgetStatus.IDLE,
    )
}

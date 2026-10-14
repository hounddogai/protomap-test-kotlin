package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor slot is in its life cycle. */
enum class EmissionFactorSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A emission factor slot as the emission factor team keeps it. */
data class EmissionFactorSlot(
    var code: String,
    var title: String,
    var depot: String,
    var pinned: Boolean,
    var lane: String,
    var fuelLiters: Double,
    var amount: Double,
    var durationMinutes: Int,
    var cost: Double,
    var windowDays: Int,
    var status: EmissionFactorSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorSlotStatus): Boolean {
        when (status) {
            EmissionFactorSlotStatus.NEW -> return next == EmissionFactorSlotStatus.QUEUED || next == EmissionFactorSlotStatus.FAILED
            EmissionFactorSlotStatus.QUEUED -> return next == EmissionFactorSlotStatus.RUNNING
            EmissionFactorSlotStatus.RUNNING -> return next == EmissionFactorSlotStatus.FAILED
            EmissionFactorSlotStatus.DONE -> return next == EmissionFactorSlotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: EmissionFactorSlotStatus) {
        if (!canMoveTo(next)) {
            error("emission factor slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedLoad(): Double = round((cost + fuelLiters) * 100) / 100

    fun snapshot(): EmissionFactorSlot {
        return EmissionFactorSlot(
            code = code,
            title = title,
            depot = depot,
            pinned = pinned,
            lane = lane,
            fuelLiters = fuelLiters,
            amount = amount,
            durationMinutes = durationMinutes,
            cost = cost,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a emission factor slot in status new. */
fun newEmissionFactorSlot(code: String, title: String): EmissionFactorSlot {
    return EmissionFactorSlot(
        code = code,
        title = title,
        depot = "",
        pinned = false,
        lane = "",
        fuelLiters = 0.0,
        amount = 0.0,
        durationMinutes = 0,
        cost = 0.0,
        windowDays = 0,
        status = EmissionFactorSlotStatus.NEW,
    )
}

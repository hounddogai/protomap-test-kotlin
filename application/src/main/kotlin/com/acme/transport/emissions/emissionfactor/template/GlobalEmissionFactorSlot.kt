package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global emission factor slot is in its life cycle. */
enum class GlobalEmissionFactorSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A global emission factor slot as the emission factor team keeps it. */
data class GlobalEmissionFactorSlot(
    var code: String,
    var name: String,
    var odometer: Int,
    var lane: String,
    var createdAt: Int,
    var revision: Int,
    var attempts: Int,
    var status: GlobalEmissionFactorSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalEmissionFactorSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalEmissionFactorSlotStatus): Boolean {
        when (status) {
            GlobalEmissionFactorSlotStatus.PLANNED -> return next == GlobalEmissionFactorSlotStatus.SCHEDULED || next == GlobalEmissionFactorSlotStatus.COMPLETED
            GlobalEmissionFactorSlotStatus.SCHEDULED -> {
                when (next) {
                    GlobalEmissionFactorSlotStatus.IN_PROGRESS, GlobalEmissionFactorSlotStatus.COMPLETED -> return true
                }
                return false
            }
            GlobalEmissionFactorSlotStatus.IN_PROGRESS -> return next == GlobalEmissionFactorSlotStatus.COMPLETED || next == GlobalEmissionFactorSlotStatus.CANCELLED
            GlobalEmissionFactorSlotStatus.COMPLETED -> {
                when (next) {
                    GlobalEmissionFactorSlotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalEmissionFactorSlotStatus) {
        if (!canMoveTo(next)) {
            error("global emission factor slot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun effectiveValue(): Double = round((revision.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): GlobalEmissionFactorSlot {
        return GlobalEmissionFactorSlot(
            code = code,
            name = name,
            odometer = odometer,
            lane = lane,
            createdAt = createdAt,
            revision = revision,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global emission factor slot in status planned. */
fun newGlobalEmissionFactorSlot(code: String, name: String): GlobalEmissionFactorSlot {
    return GlobalEmissionFactorSlot(
        code = code,
        name = name,
        odometer = 0,
        lane = "",
        createdAt = 0,
        revision = 0,
        attempts = 0,
        status = GlobalEmissionFactorSlotStatus.PLANNED,
    )
}

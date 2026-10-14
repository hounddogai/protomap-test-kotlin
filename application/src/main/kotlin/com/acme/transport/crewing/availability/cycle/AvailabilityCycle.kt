package com.acme.transport.crewing.availability.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability cycle records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability cycle is in its life cycle. */
enum class AvailabilityCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A availability cycle. Its code is unique within a tenant. */
data class AvailabilityCycle(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var etaMinutes: Int,
    var expiresAt: Int,
    var durationMinutes: Int,
    var status: AvailabilityCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityCycleStatus): Boolean {
        when (status) {
            AvailabilityCycleStatus.PLANNED -> {
                when (next) {
                    AvailabilityCycleStatus.COMPLETED -> return true
                }
                return false
            }
            AvailabilityCycleStatus.SCHEDULED -> return next == AvailabilityCycleStatus.IN_PROGRESS
            AvailabilityCycleStatus.IN_PROGRESS -> return next == AvailabilityCycleStatus.COMPLETED || next == AvailabilityCycleStatus.CANCELLED
            AvailabilityCycleStatus.COMPLETED -> return next == AvailabilityCycleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AvailabilityCycleStatus) {
        if (!canMoveTo(next)) {
            error("availability cycle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netValue(): Double = round(etaMinutes.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): AvailabilityCycle {
        return AvailabilityCycle(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            etaMinutes = etaMinutes,
            expiresAt = expiresAt,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a availability cycle in status planned. */
fun newAvailabilityCycle(code: String, name: String): AvailabilityCycle {
    return AvailabilityCycle(
        code = code,
        name = name,
        fuelLiters = 0.0,
        etaMinutes = 0,
        expiresAt = 0,
        durationMinutes = 0,
        status = AvailabilityCycleStatus.PLANNED,
    )
}

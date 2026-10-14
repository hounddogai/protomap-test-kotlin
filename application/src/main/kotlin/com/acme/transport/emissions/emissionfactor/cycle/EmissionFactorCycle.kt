package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The emission factor cycle records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a emission factor cycle is in its life cycle. */
enum class EmissionFactorCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A emission factor cycle as the emission factor team keeps it. */
data class EmissionFactorCycle(
    var code: String,
    var name: String,
    var unit: String,
    var title: String,
    var ownerId: String,
    var sequence: Int,
    var locale: String,
    var channel: String,
    var updatedAt: Int,
    var status: EmissionFactorCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorCycleStatus): Boolean {
        when (status) {
            EmissionFactorCycleStatus.OPEN -> return next == EmissionFactorCycleStatus.ARCHIVED
            EmissionFactorCycleStatus.IN_REVIEW -> {
                when (next) {
                    EmissionFactorCycleStatus.ARCHIVED -> return true
                }
                return false
            }
            EmissionFactorCycleStatus.RESOLVED -> return next == EmissionFactorCycleStatus.REOPENED || next == EmissionFactorCycleStatus.ARCHIVED
            EmissionFactorCycleStatus.REOPENED -> return next == EmissionFactorCycleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: EmissionFactorCycleStatus) {
        if (!canMoveTo(next)) {
            error("emission factor cycle $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveExposure(): Double = round(sequence.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): EmissionFactorCycle {
        return EmissionFactorCycle(
            code = code,
            name = name,
            unit = unit,
            title = title,
            ownerId = ownerId,
            sequence = sequence,
            locale = locale,
            channel = channel,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a emission factor cycle in status open. */
fun newEmissionFactorCycle(code: String, name: String): EmissionFactorCycle {
    return EmissionFactorCycle(
        code = code,
        name = name,
        unit = "",
        title = "",
        ownerId = "",
        sequence = 0,
        locale = "",
        channel = "",
        updatedAt = 0,
        status = EmissionFactorCycleStatus.OPEN,
    )
}

package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal emission factor cycle is in its life cycle. */
enum class SeasonalEmissionFactorCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A seasonal emission factor cycle. Its code is unique within a tenant. */
data class SeasonalEmissionFactorCycle(
    var code: String,
    var label: String,
    var ceiling: Double,
    var budget: Double,
    var weight: Double,
    var fuelLiters: Double,
    var version: Int,
    var summary: String,
    var sequence: Int,
    var loadKg: Double,
    var dock: String,
    var status: SeasonalEmissionFactorCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalEmissionFactorCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalEmissionFactorCycleStatus): Boolean {
        when (status) {
            SeasonalEmissionFactorCycleStatus.PROPOSED -> {
                when (next) {
                    SeasonalEmissionFactorCycleStatus.APPROVED -> return true
                }
                return false
            }
            SeasonalEmissionFactorCycleStatus.APPROVED -> {
                when (next) {
                    SeasonalEmissionFactorCycleStatus.REJECTED -> return true
                }
                return false
            }
            SeasonalEmissionFactorCycleStatus.REJECTED -> {
                when (next) {
                    SeasonalEmissionFactorCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalEmissionFactorCycleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal emission factor cycle $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"seasonal emission factor cycle"} $code"

    fun adjustedVolume(): Double = round((budget - loadKg) * 100) / 100

    fun snapshot(): SeasonalEmissionFactorCycle {
        return SeasonalEmissionFactorCycle(
            code = code,
            label = label,
            ceiling = ceiling,
            budget = budget,
            weight = weight,
            fuelLiters = fuelLiters,
            version = version,
            summary = summary,
            sequence = sequence,
            loadKg = loadKg,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal emission factor cycle in status proposed. */
fun newSeasonalEmissionFactorCycle(code: String, label: String): SeasonalEmissionFactorCycle {
    return SeasonalEmissionFactorCycle(
        code = code,
        label = label,
        ceiling = 0.0,
        budget = 0.0,
        weight = 0.0,
        fuelLiters = 0.0,
        version = 0,
        summary = "",
        sequence = 0,
        loadKg = 0.0,
        dock = "",
        status = SeasonalEmissionFactorCycleStatus.PROPOSED,
    )
}

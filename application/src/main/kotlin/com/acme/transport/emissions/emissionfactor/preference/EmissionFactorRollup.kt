package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor rollup is in its life cycle. */
enum class EmissionFactorRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A emission factor rollup as the emission factor team keeps it. */
data class EmissionFactorRollup(
    var code: String,
    var title: String,
    var threshold: Double,
    var ratio: Double,
    var rate: Double,
    var shiftCode: String,
    var unit: String,
    var cost: Double,
    var durationMinutes: Int,
    var version: Int,
    var status: EmissionFactorRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorRollupStatus): Boolean {
        when (status) {
            EmissionFactorRollupStatus.REQUESTED -> return next == EmissionFactorRollupStatus.GRANTED || next == EmissionFactorRollupStatus.REVOKED
            EmissionFactorRollupStatus.GRANTED -> {
                when (next) {
                    EmissionFactorRollupStatus.EXPIRED -> return true
                }
                return false
            }
            EmissionFactorRollupStatus.REVOKED -> {
                when (next) {
                    EmissionFactorRollupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorRollupStatus) {
        if (!canMoveTo(next)) {
            error("emission factor rollup $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun effectiveExposure(): Double = round((threshold + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorRollup {
        return EmissionFactorRollup(
            code = code,
            title = title,
            threshold = threshold,
            ratio = ratio,
            rate = rate,
            shiftCode = shiftCode,
            unit = unit,
            cost = cost,
            durationMinutes = durationMinutes,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a emission factor rollup in status requested. */
fun newEmissionFactorRollup(code: String, title: String): EmissionFactorRollup {
    return EmissionFactorRollup(
        code = code,
        title = title,
        threshold = 0.0,
        ratio = 0.0,
        rate = 0.0,
        shiftCode = "",
        unit = "",
        cost = 0.0,
        durationMinutes = 0,
        version = 0,
        status = EmissionFactorRollupStatus.REQUESTED,
    )
}

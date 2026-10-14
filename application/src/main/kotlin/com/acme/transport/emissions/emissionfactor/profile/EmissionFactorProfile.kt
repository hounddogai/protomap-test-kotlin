package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The emission factor profile records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a emission factor profile is in its life cycle. */
enum class EmissionFactorProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A emission factor profile as the emission factor team keeps it. */
data class EmissionFactorProfile(
    var code: String,
    var label: String,
    var margin: Double,
    var maximum: Double,
    var unit: String,
    var status: EmissionFactorProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorProfileStatus): Boolean {
        when (status) {
            EmissionFactorProfileStatus.NEW -> return next == EmissionFactorProfileStatus.RUNNING || next == EmissionFactorProfileStatus.DONE
            EmissionFactorProfileStatus.QUEUED -> {
                when (next) {
                    EmissionFactorProfileStatus.FAILED -> return true
                }
                return false
            }
            EmissionFactorProfileStatus.RUNNING -> return next == EmissionFactorProfileStatus.FAILED
            EmissionFactorProfileStatus.DONE -> return next == EmissionFactorProfileStatus.FAILED
        }
        return false
    }

    fun moveTo(next: EmissionFactorProfileStatus) {
        if (!canMoveTo(next)) {
            error("emission factor profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedLoad(): Double = round((maximum + margin) * 100) / 100

    fun snapshot(): EmissionFactorProfile {
        return EmissionFactorProfile(
            code = code,
            label = label,
            margin = margin,
            maximum = maximum,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a emission factor profile in status new. */
fun newEmissionFactorProfile(code: String, label: String): EmissionFactorProfile {
    return EmissionFactorProfile(
        code = code,
        label = label,
        margin = 0.0,
        maximum = 0.0,
        unit = "",
        status = EmissionFactorProfileStatus.NEW,
    )
}

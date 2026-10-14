package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor mapping is in its life cycle. */
enum class EmissionFactorMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A emission factor mapping as the emission factor team keeps it. */
data class EmissionFactorMapping(
    var code: String,
    var label: String,
    var minimum: Double,
    var loadKg: Double,
    var contactEmail: String,
    var name: String,
    var lane: String,
    var status: EmissionFactorMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorMappingStatus): Boolean {
        when (status) {
            EmissionFactorMappingStatus.PLANNED -> return next == EmissionFactorMappingStatus.COMPLETED || next == EmissionFactorMappingStatus.CANCELLED
            EmissionFactorMappingStatus.SCHEDULED -> {
                when (next) {
                    EmissionFactorMappingStatus.IN_PROGRESS -> return true
                }
                return false
            }
            EmissionFactorMappingStatus.IN_PROGRESS -> {
                when (next) {
                    EmissionFactorMappingStatus.COMPLETED -> return true
                }
                return false
            }
            EmissionFactorMappingStatus.COMPLETED -> {
                when (next) {
                    EmissionFactorMappingStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorMappingStatus) {
        if (!canMoveTo(next)) {
            error("emission factor mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedTotal(): Double = round((loadKg - minimum) * 100) / 100

    fun snapshot(): EmissionFactorMapping {
        return EmissionFactorMapping(
            code = code,
            label = label,
            minimum = minimum,
            loadKg = loadKg,
            contactEmail = contactEmail,
            name = name,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a emission factor mapping in status planned. */
fun newEmissionFactorMapping(code: String, label: String): EmissionFactorMapping {
    return EmissionFactorMapping(
        code = code,
        label = label,
        minimum = 0.0,
        loadKg = 0.0,
        contactEmail = "",
        name = "",
        lane = "",
        status = EmissionFactorMappingStatus.PLANNED,
    )
}

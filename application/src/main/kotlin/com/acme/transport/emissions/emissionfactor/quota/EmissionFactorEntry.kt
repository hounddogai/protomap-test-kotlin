package com.acme.transport.emissions.emissionfactor.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor entry is in its life cycle. */
enum class EmissionFactorEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One emission factor entry, with what the planners need to act on it. */
data class EmissionFactorEntry(
    var code: String,
    var name: String,
    var weight: Double,
    var source: String,
    var ceiling: Double,
    var updatedAt: Int,
    var country: String,
    var locale: String,
    var status: EmissionFactorEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorEntryStatus): Boolean {
        when (status) {
            EmissionFactorEntryStatus.REQUESTED -> {
                when (next) {
                    EmissionFactorEntryStatus.REVOKED -> return true
                }
                return false
            }
            EmissionFactorEntryStatus.GRANTED -> {
                when (next) {
                    EmissionFactorEntryStatus.REVOKED, EmissionFactorEntryStatus.EXPIRED -> return true
                }
                return false
            }
            EmissionFactorEntryStatus.REVOKED -> {
                when (next) {
                    EmissionFactorEntryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorEntryStatus) {
        if (!canMoveTo(next)) {
            error("emission factor entry $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netValue(): Double = round(weight * ceiling * 100) / 100

    fun snapshot(): EmissionFactorEntry {
        return EmissionFactorEntry(
            code = code,
            name = name,
            weight = weight,
            source = source,
            ceiling = ceiling,
            updatedAt = updatedAt,
            country = country,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a emission factor entry in status requested. */
fun newEmissionFactorEntry(code: String, name: String): EmissionFactorEntry {
    return EmissionFactorEntry(
        code = code,
        name = name,
        weight = 0.0,
        source = "",
        ceiling = 0.0,
        updatedAt = 0,
        country = "",
        locale = "",
        status = EmissionFactorEntryStatus.REQUESTED,
    )
}

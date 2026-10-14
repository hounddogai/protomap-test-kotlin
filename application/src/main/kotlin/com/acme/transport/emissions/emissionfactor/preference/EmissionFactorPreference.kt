package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The emission factor preference records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a emission factor preference is in its life cycle. */
enum class EmissionFactorPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One emission factor preference, with what the planners need to act on it. */
data class EmissionFactorPreference(
    var code: String,
    var label: String,
    var sequence: Int,
    var limit: Int,
    var region: String,
    var distanceKm: Double,
    var updatedAt: Int,
    var summary: String,
    var title: String,
    var status: EmissionFactorPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorPreferenceStatus): Boolean {
        when (status) {
            EmissionFactorPreferenceStatus.NEW -> {
                when (next) {
                    EmissionFactorPreferenceStatus.QUEUED, EmissionFactorPreferenceStatus.RUNNING -> return true
                }
                return false
            }
            EmissionFactorPreferenceStatus.QUEUED -> return next == EmissionFactorPreferenceStatus.DONE || next == EmissionFactorPreferenceStatus.FAILED
            EmissionFactorPreferenceStatus.RUNNING -> return next == EmissionFactorPreferenceStatus.DONE || next == EmissionFactorPreferenceStatus.FAILED
            EmissionFactorPreferenceStatus.DONE -> return next == EmissionFactorPreferenceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: EmissionFactorPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("emission factor preference $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedTotal(): Double = round(distanceKm * updatedAt.toDouble() * 100) / 100

    fun snapshot(): EmissionFactorPreference {
        return EmissionFactorPreference(
            code = code,
            label = label,
            sequence = sequence,
            limit = limit,
            region = region,
            distanceKm = distanceKm,
            updatedAt = updatedAt,
            summary = summary,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a emission factor preference in status new. */
fun newEmissionFactorPreference(code: String, label: String): EmissionFactorPreference {
    return EmissionFactorPreference(
        code = code,
        label = label,
        sequence = 0,
        limit = 0,
        region = "",
        distanceKm = 0.0,
        updatedAt = 0,
        summary = "",
        title = "",
        status = EmissionFactorPreferenceStatus.NEW,
    )
}

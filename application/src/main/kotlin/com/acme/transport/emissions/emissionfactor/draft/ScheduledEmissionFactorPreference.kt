package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled emission factor preference is in its life cycle. */
enum class ScheduledEmissionFactorPreferenceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One scheduled emission factor preference, with what the planners need to act on it. */
data class ScheduledEmissionFactorPreference(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var segment: String,
    var threshold: Double,
    var source: String,
    var status: ScheduledEmissionFactorPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledEmissionFactorPreferenceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledEmissionFactorPreferenceStatus): Boolean {
        when (status) {
            ScheduledEmissionFactorPreferenceStatus.DRAFT -> return next == ScheduledEmissionFactorPreferenceStatus.CLOSED
            ScheduledEmissionFactorPreferenceStatus.PENDING -> return next == ScheduledEmissionFactorPreferenceStatus.CLOSED
            ScheduledEmissionFactorPreferenceStatus.ACTIVE -> {
                when (next) {
                    ScheduledEmissionFactorPreferenceStatus.SUSPENDED, ScheduledEmissionFactorPreferenceStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledEmissionFactorPreferenceStatus.SUSPENDED -> return next == ScheduledEmissionFactorPreferenceStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledEmissionFactorPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("scheduled emission factor preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedVolume(): Double = round(fuelLiters * threshold * 100) / 100

    fun snapshot(): ScheduledEmissionFactorPreference {
        return ScheduledEmissionFactorPreference(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            segment = segment,
            threshold = threshold,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled emission factor preference in status draft. */
fun newScheduledEmissionFactorPreference(code: String, label: String): ScheduledEmissionFactorPreference {
    return ScheduledEmissionFactorPreference(
        code = code,
        label = label,
        fuelLiters = 0.0,
        segment = "",
        threshold = 0.0,
        source = "",
        status = ScheduledEmissionFactorPreferenceStatus.DRAFT,
    )
}

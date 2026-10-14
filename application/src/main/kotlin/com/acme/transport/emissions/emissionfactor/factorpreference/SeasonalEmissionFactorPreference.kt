package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal emission factor preference records of emissions's emission factor work, and the rules, stores,
// and calculations around them.

/** Where a seasonal emission factor preference is in its life cycle. */
enum class SeasonalEmissionFactorPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal emission factor preference. Its code is unique within a tenant. */
data class SeasonalEmissionFactorPreference(
    var code: String,
    var title: String,
    var vehicleId: String,
    var stopCount: Int,
    var threshold: Double,
    var amount: Double,
    var status: SeasonalEmissionFactorPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalEmissionFactorPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalEmissionFactorPreferenceStatus): Boolean {
        when (status) {
            SeasonalEmissionFactorPreferenceStatus.IDLE -> {
                when (next) {
                    SeasonalEmissionFactorPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalEmissionFactorPreferenceStatus.WARMING -> {
                when (next) {
                    SeasonalEmissionFactorPreferenceStatus.READY, SeasonalEmissionFactorPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalEmissionFactorPreferenceStatus.READY -> {
                when (next) {
                    SeasonalEmissionFactorPreferenceStatus.DRAINING, SeasonalEmissionFactorPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalEmissionFactorPreferenceStatus.DRAINING -> return next == SeasonalEmissionFactorPreferenceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SeasonalEmissionFactorPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("seasonal emission factor preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedVolume(): Double = round((stopCount.toDouble() - amount) * 100) / 100

    fun snapshot(): SeasonalEmissionFactorPreference {
        return SeasonalEmissionFactorPreference(
            code = code,
            title = title,
            vehicleId = vehicleId,
            stopCount = stopCount,
            threshold = threshold,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal emission factor preference in status idle. */
fun newSeasonalEmissionFactorPreference(code: String, title: String): SeasonalEmissionFactorPreference {
    return SeasonalEmissionFactorPreference(
        code = code,
        title = title,
        vehicleId = "",
        stopCount = 0,
        threshold = 0.0,
        amount = 0.0,
        status = SeasonalEmissionFactorPreferenceStatus.IDLE,
    )
}

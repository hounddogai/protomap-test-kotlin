package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal emission factor override is in its life cycle. */
enum class SeasonalEmissionFactorOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a seasonal emission factor override and its current status. */
data class SeasonalEmissionFactorOverride(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var currency: String,
    var segment: String,
    var graceDays: Int,
    var status: SeasonalEmissionFactorOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalEmissionFactorOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalEmissionFactorOverrideStatus): Boolean {
        when (status) {
            SeasonalEmissionFactorOverrideStatus.NEW -> {
                when (next) {
                    SeasonalEmissionFactorOverrideStatus.FAILED -> return true
                }
                return false
            }
            SeasonalEmissionFactorOverrideStatus.QUEUED -> return next == SeasonalEmissionFactorOverrideStatus.RUNNING || next == SeasonalEmissionFactorOverrideStatus.FAILED
            SeasonalEmissionFactorOverrideStatus.RUNNING -> {
                when (next) {
                    SeasonalEmissionFactorOverrideStatus.DONE -> return true
                }
                return false
            }
            SeasonalEmissionFactorOverrideStatus.DONE -> return next == SeasonalEmissionFactorOverrideStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SeasonalEmissionFactorOverrideStatus) {
        if (!canMoveTo(next)) {
            error("seasonal emission factor override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveVolume(): Double = round((graceDays.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): SeasonalEmissionFactorOverride {
        return SeasonalEmissionFactorOverride(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            currency = currency,
            segment = segment,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal emission factor override in status new. */
fun newSeasonalEmissionFactorOverride(code: String, label: String): SeasonalEmissionFactorOverride {
    return SeasonalEmissionFactorOverride(
        code = code,
        label = label,
        etaMinutes = 0,
        currency = "",
        segment = "",
        graceDays = 0,
        status = SeasonalEmissionFactorOverrideStatus.NEW,
    )
}

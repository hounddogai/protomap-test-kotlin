package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import com.acme.transport.kit.text.normalize

/** Where a seasonal emission factor exception is in its life cycle. */
enum class SeasonalEmissionFactorExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal emission factor exception as the emission factor team keeps it. */
data class SeasonalEmissionFactorException(
    var code: String,
    var label: String,
    var trailerId: String,
    var distanceKm: Double,
    var attributes: MutableMap<String, String>,
    var status: SeasonalEmissionFactorExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalEmissionFactorExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalEmissionFactorExceptionStatus): Boolean {
        when (status) {
            SeasonalEmissionFactorExceptionStatus.NEW -> {
                when (next) {
                    SeasonalEmissionFactorExceptionStatus.FAILED -> return true
                }
                return false
            }
            SeasonalEmissionFactorExceptionStatus.QUEUED -> return next == SeasonalEmissionFactorExceptionStatus.DONE
            SeasonalEmissionFactorExceptionStatus.RUNNING -> {
                when (next) {
                    SeasonalEmissionFactorExceptionStatus.FAILED -> return true
                }
                return false
            }
            SeasonalEmissionFactorExceptionStatus.DONE -> return next == SeasonalEmissionFactorExceptionStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SeasonalEmissionFactorExceptionStatus) {
        if (!canMoveTo(next)) {
            error("seasonal emission factor exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal emission factor exception"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = distanceKm

    fun snapshot(): SeasonalEmissionFactorException {
        return SeasonalEmissionFactorException(
            code = code,
            label = label,
            trailerId = trailerId,
            distanceKm = distanceKm,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal emission factor exception in status new. */
fun newSeasonalEmissionFactorException(code: String, label: String): SeasonalEmissionFactorException {
    return SeasonalEmissionFactorException(
        code = code,
        label = label,
        trailerId = "",
        distanceKm = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = SeasonalEmissionFactorExceptionStatus.NEW,
    )
}

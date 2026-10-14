package com.acme.transport.emissions.emissionfactor.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor exception is in its life cycle. */
enum class EmissionFactorExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a emission factor exception and its current status. */
data class EmissionFactorException(
    var code: String,
    var label: String,
    var margin: Double,
    var minimum: Double,
    var category: String,
    var status: EmissionFactorExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorExceptionStatus): Boolean {
        when (status) {
            EmissionFactorExceptionStatus.IDLE -> {
                when (next) {
                    EmissionFactorExceptionStatus.READY -> return true
                }
                return false
            }
            EmissionFactorExceptionStatus.WARMING -> {
                when (next) {
                    EmissionFactorExceptionStatus.READY, EmissionFactorExceptionStatus.DRAINING -> return true
                }
                return false
            }
            EmissionFactorExceptionStatus.READY -> {
                when (next) {
                    EmissionFactorExceptionStatus.DRAINING -> return true
                }
                return false
            }
            EmissionFactorExceptionStatus.DRAINING -> {
                when (next) {
                    EmissionFactorExceptionStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorExceptionStatus) {
        if (!canMoveTo(next)) {
            error("emission factor exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round((minimum + margin) * 100) / 100

    fun snapshot(): EmissionFactorException {
        return EmissionFactorException(
            code = code,
            label = label,
            margin = margin,
            minimum = minimum,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a emission factor exception in status idle. */
fun newEmissionFactorException(code: String, label: String): EmissionFactorException {
    return EmissionFactorException(
        code = code,
        label = label,
        margin = 0.0,
        minimum = 0.0,
        category = "",
        status = EmissionFactorExceptionStatus.IDLE,
    )
}

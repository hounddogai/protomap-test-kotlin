package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The emission factor request records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a emission factor request is in its life cycle. */
enum class EmissionFactorRequestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a emission factor request and its current status. */
data class EmissionFactorRequest(
    var code: String,
    var label: String,
    var source: String,
    var batchSize: Int,
    var ratio: Double,
    var carrier: String,
    var etaMinutes: Int,
    var country: String,
    var version: Int,
    var status: EmissionFactorRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorRequestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorRequestStatus): Boolean {
        when (status) {
            EmissionFactorRequestStatus.PLANNED -> return next == EmissionFactorRequestStatus.SCHEDULED || next == EmissionFactorRequestStatus.CANCELLED
            EmissionFactorRequestStatus.SCHEDULED -> {
                when (next) {
                    EmissionFactorRequestStatus.COMPLETED -> return true
                }
                return false
            }
            EmissionFactorRequestStatus.IN_PROGRESS -> {
                when (next) {
                    EmissionFactorRequestStatus.CANCELLED -> return true
                }
                return false
            }
            EmissionFactorRequestStatus.COMPLETED -> return next == EmissionFactorRequestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: EmissionFactorRequestStatus) {
        if (!canMoveTo(next)) {
            error("emission factor request $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"emission factor request"} $code"

    fun netExposure(): Double = round((version.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorRequest {
        return EmissionFactorRequest(
            code = code,
            label = label,
            source = source,
            batchSize = batchSize,
            ratio = ratio,
            carrier = carrier,
            etaMinutes = etaMinutes,
            country = country,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a emission factor request in status planned. */
fun newEmissionFactorRequest(code: String, label: String): EmissionFactorRequest {
    return EmissionFactorRequest(
        code = code,
        label = label,
        source = "",
        batchSize = 0,
        ratio = 0.0,
        carrier = "",
        etaMinutes = 0,
        country = "",
        version = 0,
        status = EmissionFactorRequestStatus.PLANNED,
    )
}

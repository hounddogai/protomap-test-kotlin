package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The emission factor assignment records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a emission factor assignment is in its life cycle. */
enum class EmissionFactorAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A emission factor assignment. Its code is unique within a tenant. */
data class EmissionFactorAssignment(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var distanceKm: Double,
    var score: Double,
    var etaMinutes: Int,
    var ceiling: Double,
    var title: String,
    var driverId: String,
    var currency: String,
    var odometer: Int,
    var status: EmissionFactorAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorAssignmentStatus): Boolean {
        when (status) {
            EmissionFactorAssignmentStatus.REQUESTED -> return next == EmissionFactorAssignmentStatus.GRANTED || next == EmissionFactorAssignmentStatus.EXPIRED
            EmissionFactorAssignmentStatus.GRANTED -> {
                when (next) {
                    EmissionFactorAssignmentStatus.REVOKED, EmissionFactorAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            EmissionFactorAssignmentStatus.REVOKED -> return next == EmissionFactorAssignmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: EmissionFactorAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("emission factor assignment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round((distanceKm + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorAssignment {
        return EmissionFactorAssignment(
            code = code,
            name = name,
            expiresAt = expiresAt,
            distanceKm = distanceKm,
            score = score,
            etaMinutes = etaMinutes,
            ceiling = ceiling,
            title = title,
            driverId = driverId,
            currency = currency,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a emission factor assignment in status requested. */
fun newEmissionFactorAssignment(code: String, name: String): EmissionFactorAssignment {
    return EmissionFactorAssignment(
        code = code,
        name = name,
        expiresAt = 0,
        distanceKm = 0.0,
        score = 0.0,
        etaMinutes = 0,
        ceiling = 0.0,
        title = "",
        driverId = "",
        currency = "",
        odometer = 0,
        status = EmissionFactorAssignmentStatus.REQUESTED,
    )
}

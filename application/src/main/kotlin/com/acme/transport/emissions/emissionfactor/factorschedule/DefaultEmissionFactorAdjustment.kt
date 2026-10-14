package com.acme.transport.emissions.emissionfactor.factorschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default emission factor adjustment is in its life cycle. */
enum class DefaultEmissionFactorAdjustmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One default emission factor adjustment, with what the planners need to act on it. */
data class DefaultEmissionFactorAdjustment(
    var code: String,
    var name: String,
    var startsAt: Int,
    var distanceKm: Double,
    var revision: Int,
    var trailerId: String,
    var vehicleId: String,
    var minimum: Double,
    var status: DefaultEmissionFactorAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultEmissionFactorAdjustmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultEmissionFactorAdjustmentStatus): Boolean {
        when (status) {
            DefaultEmissionFactorAdjustmentStatus.DRAFT -> {
                when (next) {
                    DefaultEmissionFactorAdjustmentStatus.SUSPENDED -> return true
                }
                return false
            }
            DefaultEmissionFactorAdjustmentStatus.PENDING -> return next == DefaultEmissionFactorAdjustmentStatus.SUSPENDED
            DefaultEmissionFactorAdjustmentStatus.ACTIVE -> {
                when (next) {
                    DefaultEmissionFactorAdjustmentStatus.SUSPENDED, DefaultEmissionFactorAdjustmentStatus.CLOSED -> return true
                }
                return false
            }
            DefaultEmissionFactorAdjustmentStatus.SUSPENDED -> return next == DefaultEmissionFactorAdjustmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DefaultEmissionFactorAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("default emission factor adjustment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"default emission factor adjustment"} $code"

    fun weightedExposure(): Double = round((distanceKm + startsAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultEmissionFactorAdjustment {
        return DefaultEmissionFactorAdjustment(
            code = code,
            name = name,
            startsAt = startsAt,
            distanceKm = distanceKm,
            revision = revision,
            trailerId = trailerId,
            vehicleId = vehicleId,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default emission factor adjustment in status draft. */
fun newDefaultEmissionFactorAdjustment(code: String, name: String): DefaultEmissionFactorAdjustment {
    return DefaultEmissionFactorAdjustment(
        code = code,
        name = name,
        startsAt = 0,
        distanceKm = 0.0,
        revision = 0,
        trailerId = "",
        vehicleId = "",
        minimum = 0.0,
        status = DefaultEmissionFactorAdjustmentStatus.DRAFT,
    )
}

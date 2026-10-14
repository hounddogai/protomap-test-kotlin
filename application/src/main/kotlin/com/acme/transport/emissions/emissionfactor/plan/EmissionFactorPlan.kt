package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The emission factor plan records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a emission factor plan is in its life cycle. */
enum class EmissionFactorPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A emission factor plan as the emission factor team keeps it. */
data class EmissionFactorPlan(
    var code: String,
    var name: String,
    var contactEmail: String,
    var ownerId: String,
    var archived: Boolean,
    var windowDays: Int,
    var distanceKm: Double,
    var status: EmissionFactorPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorPlanStatus): Boolean {
        when (status) {
            EmissionFactorPlanStatus.IDLE -> return next == EmissionFactorPlanStatus.READY
            EmissionFactorPlanStatus.WARMING -> return next == EmissionFactorPlanStatus.STOPPED
            EmissionFactorPlanStatus.READY -> {
                when (next) {
                    EmissionFactorPlanStatus.DRAINING -> return true
                }
                return false
            }
            EmissionFactorPlanStatus.DRAINING -> {
                when (next) {
                    EmissionFactorPlanStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorPlanStatus) {
        if (!canMoveTo(next)) {
            error("emission factor plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netValue(): Double = round((windowDays.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): EmissionFactorPlan {
        return EmissionFactorPlan(
            code = code,
            name = name,
            contactEmail = contactEmail,
            ownerId = ownerId,
            archived = archived,
            windowDays = windowDays,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a emission factor plan in status idle. */
fun newEmissionFactorPlan(code: String, name: String): EmissionFactorPlan {
    return EmissionFactorPlan(
        code = code,
        name = name,
        contactEmail = "",
        ownerId = "",
        archived = false,
        windowDays = 0,
        distanceKm = 0.0,
        status = EmissionFactorPlanStatus.IDLE,
    )
}

package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted lane plan window records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a weighted lane plan window is in its life cycle. */
enum class WeightedLanePlanWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted lane plan window. Its code is unique within a tenant. */
data class WeightedLanePlanWindow(
    var code: String,
    var name: String,
    var contactEmail: String,
    var weight: Double,
    var currency: String,
    var fuelLiters: Double,
    var status: WeightedLanePlanWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedLanePlanWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedLanePlanWindowStatus): Boolean {
        when (status) {
            WeightedLanePlanWindowStatus.DRAFT -> return next == WeightedLanePlanWindowStatus.ACTIVE || next == WeightedLanePlanWindowStatus.CLOSED
            WeightedLanePlanWindowStatus.PENDING -> return next == WeightedLanePlanWindowStatus.ACTIVE
            WeightedLanePlanWindowStatus.ACTIVE -> return next == WeightedLanePlanWindowStatus.SUSPENDED || next == WeightedLanePlanWindowStatus.CLOSED
            WeightedLanePlanWindowStatus.SUSPENDED -> return next == WeightedLanePlanWindowStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: WeightedLanePlanWindowStatus) {
        if (!canMoveTo(next)) {
            error("weighted lane plan window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveValue(): Double = round((weight - fuelLiters) * 100) / 100

    fun snapshot(): WeightedLanePlanWindow {
        return WeightedLanePlanWindow(
            code = code,
            name = name,
            contactEmail = contactEmail,
            weight = weight,
            currency = currency,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted lane plan window in status draft. */
fun newWeightedLanePlanWindow(code: String, name: String): WeightedLanePlanWindow {
    return WeightedLanePlanWindow(
        code = code,
        name = name,
        contactEmail = "",
        weight = 0.0,
        currency = "",
        fuelLiters = 0.0,
        status = WeightedLanePlanWindowStatus.DRAFT,
    )
}

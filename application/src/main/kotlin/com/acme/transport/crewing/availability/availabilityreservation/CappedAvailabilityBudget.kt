package com.acme.transport.crewing.availability.availabilityreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped availability budget is in its life cycle. */
enum class CappedAvailabilityBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capped availability budget as the availability team keeps it. */
data class CappedAvailabilityBudget(
    var code: String,
    var title: String,
    var graceDays: Int,
    var rate: Double,
    var fuelLiters: Double,
    var ownerId: String,
    var depot: String,
    var source: String,
    var active: Boolean,
    var currency: String,
    var status: CappedAvailabilityBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAvailabilityBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAvailabilityBudgetStatus): Boolean {
        when (status) {
            CappedAvailabilityBudgetStatus.OPEN -> return next == CappedAvailabilityBudgetStatus.RESOLVED
            CappedAvailabilityBudgetStatus.IN_REVIEW -> {
                when (next) {
                    CappedAvailabilityBudgetStatus.RESOLVED -> return true
                }
                return false
            }
            CappedAvailabilityBudgetStatus.RESOLVED -> {
                when (next) {
                    CappedAvailabilityBudgetStatus.REOPENED, CappedAvailabilityBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedAvailabilityBudgetStatus.REOPENED -> {
                when (next) {
                    CappedAvailabilityBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedAvailabilityBudgetStatus) {
        if (!canMoveTo(next)) {
            error("capped availability budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netExposure(): Double = round((fuelLiters - graceDays.toDouble()) * 100) / 100

    fun snapshot(): CappedAvailabilityBudget {
        return CappedAvailabilityBudget(
            code = code,
            title = title,
            graceDays = graceDays,
            rate = rate,
            fuelLiters = fuelLiters,
            ownerId = ownerId,
            depot = depot,
            source = source,
            active = active,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a capped availability budget in status open. */
fun newCappedAvailabilityBudget(code: String, title: String): CappedAvailabilityBudget {
    return CappedAvailabilityBudget(
        code = code,
        title = title,
        graceDays = 0,
        rate = 0.0,
        fuelLiters = 0.0,
        ownerId = "",
        depot = "",
        source = "",
        active = false,
        currency = "",
        status = CappedAvailabilityBudgetStatus.OPEN,
    )
}

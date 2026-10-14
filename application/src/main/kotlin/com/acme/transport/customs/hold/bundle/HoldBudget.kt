package com.acme.transport.customs.hold.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold budget is in its life cycle. */
enum class HoldBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A hold budget as the hold team keeps it. */
data class HoldBudget(
    var code: String,
    var name: String,
    var archived: Boolean,
    var vehicleId: String,
    var source: String,
    var minimum: Double,
    var quantity: Int,
    var carrier: String,
    var trailerId: String,
    var reviewer: String,
    var maximum: Double,
    var status: HoldBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldBudgetStatus): Boolean {
        when (status) {
            HoldBudgetStatus.CREATED -> return next == HoldBudgetStatus.RETIRED
            HoldBudgetStatus.VALIDATED -> {
                when (next) {
                    HoldBudgetStatus.PUBLISHED -> return true
                }
                return false
            }
            HoldBudgetStatus.PUBLISHED -> return next == HoldBudgetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: HoldBudgetStatus) {
        if (!canMoveTo(next)) {
            error("hold budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedExposure(): Double = round(quantity.toDouble() * maximum * 100) / 100

    fun snapshot(): HoldBudget {
        return HoldBudget(
            code = code,
            name = name,
            archived = archived,
            vehicleId = vehicleId,
            source = source,
            minimum = minimum,
            quantity = quantity,
            carrier = carrier,
            trailerId = trailerId,
            reviewer = reviewer,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a hold budget in status created. */
fun newHoldBudget(code: String, name: String): HoldBudget {
    return HoldBudget(
        code = code,
        name = name,
        archived = false,
        vehicleId = "",
        source = "",
        minimum = 0.0,
        quantity = 0,
        carrier = "",
        trailerId = "",
        reviewer = "",
        maximum = 0.0,
        status = HoldBudgetStatus.CREATED,
    )
}

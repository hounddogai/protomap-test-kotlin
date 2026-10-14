package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lane plan budget records of linehaul's lane plan work, and the rules, stores, and calculations around
// them.

/** Where a lane plan budget is in its life cycle. */
enum class LanePlanBudgetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a lane plan budget and its current status. */
data class LanePlanBudget(
    var code: String,
    var name: String,
    var title: String,
    var vehicleId: String,
    var quantity: Int,
    var endsAt: Int,
    var approved: Boolean,
    var updatedAt: Int,
    var currency: String,
    var externalRef: String,
    var maximum: Double,
    var status: LanePlanBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanBudgetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanBudgetStatus): Boolean {
        when (status) {
            LanePlanBudgetStatus.PROPOSED -> {
                when (next) {
                    LanePlanBudgetStatus.APPROVED, LanePlanBudgetStatus.REJECTED -> return true
                }
                return false
            }
            LanePlanBudgetStatus.APPROVED -> return next == LanePlanBudgetStatus.WITHDRAWN
            LanePlanBudgetStatus.REJECTED -> {
                when (next) {
                    LanePlanBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanBudgetStatus) {
        if (!canMoveTo(next)) {
            error("lane plan budget $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round((endsAt.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): LanePlanBudget {
        return LanePlanBudget(
            code = code,
            name = name,
            title = title,
            vehicleId = vehicleId,
            quantity = quantity,
            endsAt = endsAt,
            approved = approved,
            updatedAt = updatedAt,
            currency = currency,
            externalRef = externalRef,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lane plan budget in status proposed. */
fun newLanePlanBudget(code: String, name: String): LanePlanBudget {
    return LanePlanBudget(
        code = code,
        name = name,
        title = "",
        vehicleId = "",
        quantity = 0,
        endsAt = 0,
        approved = false,
        updatedAt = 0,
        currency = "",
        externalRef = "",
        maximum = 0.0,
        status = LanePlanBudgetStatus.PROPOSED,
    )
}

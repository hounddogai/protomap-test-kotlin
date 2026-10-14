package com.acme.transport.customs.hold.holdbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional hold budget is in its life cycle. */
enum class RegionalHoldBudgetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A regional hold budget as the hold team keeps it. */
data class RegionalHoldBudget(
    var code: String,
    var name: String,
    var depot: String,
    var routeCode: String,
    var stopCount: Int,
    var discount: Double,
    var shiftCode: String,
    var expiresAt: Int,
    var revision: Int,
    var version: Int,
    var fuelLiters: Double,
    var status: RegionalHoldBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalHoldBudgetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalHoldBudgetStatus): Boolean {
        when (status) {
            RegionalHoldBudgetStatus.PROPOSED -> {
                when (next) {
                    RegionalHoldBudgetStatus.APPROVED, RegionalHoldBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalHoldBudgetStatus.APPROVED -> {
                when (next) {
                    RegionalHoldBudgetStatus.REJECTED, RegionalHoldBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalHoldBudgetStatus.REJECTED -> return next == RegionalHoldBudgetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RegionalHoldBudgetStatus) {
        if (!canMoveTo(next)) {
            error("regional hold budget $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"regional hold budget"} $code"

    fun effectiveLoad(): Double = round((discount - revision.toDouble()) * 100) / 100

    fun snapshot(): RegionalHoldBudget {
        return RegionalHoldBudget(
            code = code,
            name = name,
            depot = depot,
            routeCode = routeCode,
            stopCount = stopCount,
            discount = discount,
            shiftCode = shiftCode,
            expiresAt = expiresAt,
            revision = revision,
            version = version,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a regional hold budget in status proposed. */
fun newRegionalHoldBudget(code: String, name: String): RegionalHoldBudget {
    return RegionalHoldBudget(
        code = code,
        name = name,
        depot = "",
        routeCode = "",
        stopCount = 0,
        discount = 0.0,
        shiftCode = "",
        expiresAt = 0,
        revision = 0,
        version = 0,
        fuelLiters = 0.0,
        status = RegionalHoldBudgetStatus.PROPOSED,
    )
}

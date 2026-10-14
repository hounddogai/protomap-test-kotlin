package com.acme.transport.freightpay.accessorial.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled accessorial budget records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a scheduled accessorial budget is in its life cycle. */
enum class ScheduledAccessorialBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One scheduled accessorial budget, with what the planners need to act on it. */
data class ScheduledAccessorialBudget(
    var code: String,
    var title: String,
    var carrier: String,
    var limit: Int,
    var graceDays: Int,
    var country: String,
    var driverId: String,
    var vehicleId: String,
    var expiresAt: Int,
    var status: ScheduledAccessorialBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAccessorialBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAccessorialBudgetStatus): Boolean {
        when (status) {
            ScheduledAccessorialBudgetStatus.REQUESTED -> return next == ScheduledAccessorialBudgetStatus.REVOKED
            ScheduledAccessorialBudgetStatus.GRANTED -> {
                when (next) {
                    ScheduledAccessorialBudgetStatus.REVOKED -> return true
                }
                return false
            }
            ScheduledAccessorialBudgetStatus.REVOKED -> {
                when (next) {
                    ScheduledAccessorialBudgetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledAccessorialBudgetStatus) {
        if (!canMoveTo(next)) {
            error("scheduled accessorial budget $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled accessorial budget"} $code"

    fun weightedValue(): Double = round((limit.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledAccessorialBudget {
        return ScheduledAccessorialBudget(
            code = code,
            title = title,
            carrier = carrier,
            limit = limit,
            graceDays = graceDays,
            country = country,
            driverId = driverId,
            vehicleId = vehicleId,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled accessorial budget in status requested. */
fun newScheduledAccessorialBudget(code: String, title: String): ScheduledAccessorialBudget {
    return ScheduledAccessorialBudget(
        code = code,
        title = title,
        carrier = "",
        limit = 0,
        graceDays = 0,
        country = "",
        driverId = "",
        vehicleId = "",
        expiresAt = 0,
        status = ScheduledAccessorialBudgetStatus.REQUESTED,
    )
}

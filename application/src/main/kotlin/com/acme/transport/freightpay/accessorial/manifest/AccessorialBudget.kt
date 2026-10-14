package com.acme.transport.freightpay.accessorial.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial budget is in its life cycle. */
enum class AccessorialBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A accessorial budget. Its code is unique within a tenant. */
data class AccessorialBudget(
    var code: String,
    var title: String,
    var currency: String,
    var floorValue: Double,
    var etaMinutes: Int,
    var threshold: Double,
    var cost: Double,
    var attempts: Int,
    var status: AccessorialBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialBudgetStatus): Boolean {
        when (status) {
            AccessorialBudgetStatus.REQUESTED -> return next == AccessorialBudgetStatus.REVOKED
            AccessorialBudgetStatus.GRANTED -> return next == AccessorialBudgetStatus.REVOKED || next == AccessorialBudgetStatus.EXPIRED
            AccessorialBudgetStatus.REVOKED -> return next == AccessorialBudgetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AccessorialBudgetStatus) {
        if (!canMoveTo(next)) {
            error("accessorial budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round(etaMinutes.toDouble() * cost * 100) / 100

    fun snapshot(): AccessorialBudget {
        return AccessorialBudget(
            code = code,
            title = title,
            currency = currency,
            floorValue = floorValue,
            etaMinutes = etaMinutes,
            threshold = threshold,
            cost = cost,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a accessorial budget in status requested. */
fun newAccessorialBudget(code: String, title: String): AccessorialBudget {
    return AccessorialBudget(
        code = code,
        title = title,
        currency = "",
        floorValue = 0.0,
        etaMinutes = 0,
        threshold = 0.0,
        cost = 0.0,
        attempts = 0,
        status = AccessorialBudgetStatus.REQUESTED,
    )
}

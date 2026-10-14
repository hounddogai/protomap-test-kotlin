package com.acme.transport.safety.violation.violationcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional violation budget is in its life cycle. */
enum class RegionalViolationBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional violation budget. Its code is unique within a tenant. */
data class RegionalViolationBudget(
    var code: String,
    var name: String,
    var capacity: Int,
    var ownerId: String,
    var carrier: String,
    var rate: Double,
    var currency: String,
    var dock: String,
    var status: RegionalViolationBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalViolationBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalViolationBudgetStatus): Boolean {
        when (status) {
            RegionalViolationBudgetStatus.CREATED -> {
                when (next) {
                    RegionalViolationBudgetStatus.VALIDATED -> return true
                }
                return false
            }
            RegionalViolationBudgetStatus.VALIDATED -> return next == RegionalViolationBudgetStatus.PUBLISHED || next == RegionalViolationBudgetStatus.RETIRED
            RegionalViolationBudgetStatus.PUBLISHED -> {
                when (next) {
                    RegionalViolationBudgetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalViolationBudgetStatus) {
        if (!canMoveTo(next)) {
            error("regional violation budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional violation budget"} $code"

    fun weightedTotal(): Double = round((rate - capacity.toDouble()) * 100) / 100

    fun snapshot(): RegionalViolationBudget {
        return RegionalViolationBudget(
            code = code,
            name = name,
            capacity = capacity,
            ownerId = ownerId,
            carrier = carrier,
            rate = rate,
            currency = currency,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional violation budget in status created. */
fun newRegionalViolationBudget(code: String, name: String): RegionalViolationBudget {
    return RegionalViolationBudget(
        code = code,
        name = name,
        capacity = 0,
        ownerId = "",
        carrier = "",
        rate = 0.0,
        currency = "",
        dock = "",
        status = RegionalViolationBudgetStatus.CREATED,
    )
}

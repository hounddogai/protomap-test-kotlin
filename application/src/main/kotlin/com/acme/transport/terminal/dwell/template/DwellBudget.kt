package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.text.normalize

/** Where a dwell budget is in its life cycle. */
enum class DwellBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A dwell budget. Its code is unique within a tenant. */
data class DwellBudget(
    var code: String,
    var name: String,
    var category: String,
    var contactEmail: String,
    var country: String,
    var trailerId: String,
    var discount: Double,
    var title: String,
    var status: DwellBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellBudgetStatus): Boolean {
        when (status) {
            DwellBudgetStatus.REQUESTED -> return next == DwellBudgetStatus.EXPIRED
            DwellBudgetStatus.GRANTED -> return next == DwellBudgetStatus.EXPIRED
            DwellBudgetStatus.REVOKED -> {
                when (next) {
                    DwellBudgetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellBudgetStatus) {
        if (!canMoveTo(next)) {
            error("dwell budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = discount

    fun snapshot(): DwellBudget {
        return DwellBudget(
            code = code,
            name = name,
            category = category,
            contactEmail = contactEmail,
            country = country,
            trailerId = trailerId,
            discount = discount,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a dwell budget in status requested. */
fun newDwellBudget(code: String, name: String): DwellBudget {
    return DwellBudget(
        code = code,
        name = name,
        category = "",
        contactEmail = "",
        country = "",
        trailerId = "",
        discount = 0.0,
        title = "",
        status = DwellBudgetStatus.REQUESTED,
    )
}

package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.kit.text.normalize

/** Where a tender budget is in its life cycle. */
enum class TenderBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tender budget as the tender team keeps it. */
data class TenderBudget(
    var code: String,
    var label: String,
    var checksum: String,
    var name: String,
    var routeCode: String,
    var depot: String,
    var driverId: String,
    var createdAt: Int,
    var status: TenderBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderBudgetStatus): Boolean {
        when (status) {
            TenderBudgetStatus.OPEN -> return next == TenderBudgetStatus.RESOLVED
            TenderBudgetStatus.IN_REVIEW -> return next == TenderBudgetStatus.RESOLVED || next == TenderBudgetStatus.ARCHIVED
            TenderBudgetStatus.RESOLVED -> return next == TenderBudgetStatus.REOPENED
            TenderBudgetStatus.REOPENED -> return next == TenderBudgetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TenderBudgetStatus) {
        if (!canMoveTo(next)) {
            error("tender budget $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): TenderBudget {
        return TenderBudget(
            code = code,
            label = label,
            checksum = checksum,
            name = name,
            routeCode = routeCode,
            depot = depot,
            driverId = driverId,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tender budget in status open. */
fun newTenderBudget(code: String, label: String): TenderBudget {
    return TenderBudget(
        code = code,
        label = label,
        checksum = "",
        name = "",
        routeCode = "",
        depot = "",
        driverId = "",
        createdAt = 0,
        status = TenderBudgetStatus.OPEN,
    )
}

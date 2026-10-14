package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.text.normalize

/** Where a manual rest break budget is in its life cycle. */
enum class ManualRestBreakBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual rest break budget. Its code is unique within a tenant. */
data class ManualRestBreakBudget(
    var code: String,
    var label: String,
    var approved: Boolean,
    var updatedAt: Int,
    var carrier: String,
    var summary: String,
    var status: ManualRestBreakBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRestBreakBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRestBreakBudgetStatus): Boolean {
        when (status) {
            ManualRestBreakBudgetStatus.CREATED -> return next == ManualRestBreakBudgetStatus.PUBLISHED || next == ManualRestBreakBudgetStatus.RETIRED
            ManualRestBreakBudgetStatus.VALIDATED -> {
                when (next) {
                    ManualRestBreakBudgetStatus.RETIRED -> return true
                }
                return false
            }
            ManualRestBreakBudgetStatus.PUBLISHED -> return next == ManualRestBreakBudgetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualRestBreakBudgetStatus) {
        if (!canMoveTo(next)) {
            error("manual rest break budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): ManualRestBreakBudget {
        return ManualRestBreakBudget(
            code = code,
            label = label,
            approved = approved,
            updatedAt = updatedAt,
            carrier = carrier,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual rest break budget in status created. */
fun newManualRestBreakBudget(code: String, label: String): ManualRestBreakBudget {
    return ManualRestBreakBudget(
        code = code,
        label = label,
        approved = false,
        updatedAt = 0,
        carrier = "",
        summary = "",
        status = ManualRestBreakBudgetStatus.CREATED,
    )
}

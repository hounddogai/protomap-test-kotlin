package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.text.normalize

/** Where a batched cutoff budget is in its life cycle. */
enum class BatchedCutoffBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A batched cutoff budget. Its code is unique within a tenant. */
data class BatchedCutoffBudget(
    var code: String,
    var title: String,
    var contactEmail: String,
    var locale: String,
    var stopCount: Int,
    var label: String,
    var pinned: Boolean,
    var status: BatchedCutoffBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCutoffBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCutoffBudgetStatus): Boolean {
        when (status) {
            BatchedCutoffBudgetStatus.REQUESTED -> return next == BatchedCutoffBudgetStatus.GRANTED
            BatchedCutoffBudgetStatus.GRANTED -> return next == BatchedCutoffBudgetStatus.REVOKED || next == BatchedCutoffBudgetStatus.EXPIRED
            BatchedCutoffBudgetStatus.REVOKED -> return next == BatchedCutoffBudgetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedCutoffBudgetStatus) {
        if (!canMoveTo(next)) {
            error("batched cutoff budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): BatchedCutoffBudget {
        return BatchedCutoffBudget(
            code = code,
            title = title,
            contactEmail = contactEmail,
            locale = locale,
            stopCount = stopCount,
            label = label,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a batched cutoff budget in status requested. */
fun newBatchedCutoffBudget(code: String, title: String): BatchedCutoffBudget {
    return BatchedCutoffBudget(
        code = code,
        title = title,
        contactEmail = "",
        locale = "",
        stopCount = 0,
        label = "",
        pinned = false,
        status = BatchedCutoffBudgetStatus.REQUESTED,
    )
}

package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual camera event budget is in its life cycle. */
enum class ManualCameraEventBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A manual camera event budget. Its code is unique within a tenant. */
data class ManualCameraEventBudget(
    var code: String,
    var title: String,
    var notes: String,
    var depot: String,
    var dock: String,
    var sequence: Int,
    var retries: Int,
    var threshold: Double,
    var ceiling: Double,
    var unit: String,
    var maximum: Double,
    var status: ManualCameraEventBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCameraEventBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCameraEventBudgetStatus): Boolean {
        when (status) {
            ManualCameraEventBudgetStatus.OPEN -> {
                when (next) {
                    ManualCameraEventBudgetStatus.REOPENED -> return true
                }
                return false
            }
            ManualCameraEventBudgetStatus.IN_REVIEW -> return next == ManualCameraEventBudgetStatus.RESOLVED || next == ManualCameraEventBudgetStatus.ARCHIVED
            ManualCameraEventBudgetStatus.RESOLVED -> return next == ManualCameraEventBudgetStatus.ARCHIVED
            ManualCameraEventBudgetStatus.REOPENED -> return next == ManualCameraEventBudgetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ManualCameraEventBudgetStatus) {
        if (!canMoveTo(next)) {
            error("manual camera event budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedTotal(): Double = round((maximum - ceiling) * 100) / 100

    fun snapshot(): ManualCameraEventBudget {
        return ManualCameraEventBudget(
            code = code,
            title = title,
            notes = notes,
            depot = depot,
            dock = dock,
            sequence = sequence,
            retries = retries,
            threshold = threshold,
            ceiling = ceiling,
            unit = unit,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual camera event budget in status open. */
fun newManualCameraEventBudget(code: String, title: String): ManualCameraEventBudget {
    return ManualCameraEventBudget(
        code = code,
        title = title,
        notes = "",
        depot = "",
        dock = "",
        sequence = 0,
        retries = 0,
        threshold = 0.0,
        ceiling = 0.0,
        unit = "",
        maximum = 0.0,
        status = ManualCameraEventBudgetStatus.OPEN,
    )
}

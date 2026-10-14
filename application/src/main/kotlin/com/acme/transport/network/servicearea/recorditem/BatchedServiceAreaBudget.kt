package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched service area budget is in its life cycle. */
enum class BatchedServiceAreaBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A batched service area budget. Its code is unique within a tenant. */
data class BatchedServiceAreaBudget(
    var code: String,
    var title: String,
    var contactEmail: String,
    var revision: Int,
    var ownerId: String,
    var threshold: Double,
    var windowDays: Int,
    var odometer: Int,
    var status: BatchedServiceAreaBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedServiceAreaBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedServiceAreaBudgetStatus): Boolean {
        when (status) {
            BatchedServiceAreaBudgetStatus.DRAFT -> {
                when (next) {
                    BatchedServiceAreaBudgetStatus.PENDING, BatchedServiceAreaBudgetStatus.ACTIVE -> return true
                }
                return false
            }
            BatchedServiceAreaBudgetStatus.PENDING -> return next == BatchedServiceAreaBudgetStatus.ACTIVE || next == BatchedServiceAreaBudgetStatus.SUSPENDED
            BatchedServiceAreaBudgetStatus.ACTIVE -> return next == BatchedServiceAreaBudgetStatus.SUSPENDED || next == BatchedServiceAreaBudgetStatus.CLOSED
            BatchedServiceAreaBudgetStatus.SUSPENDED -> {
                when (next) {
                    BatchedServiceAreaBudgetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedServiceAreaBudgetStatus) {
        if (!canMoveTo(next)) {
            error("batched service area budget $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun adjustedExposure(): Double = round((odometer.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): BatchedServiceAreaBudget {
        return BatchedServiceAreaBudget(
            code = code,
            title = title,
            contactEmail = contactEmail,
            revision = revision,
            ownerId = ownerId,
            threshold = threshold,
            windowDays = windowDays,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched service area budget in status draft. */
fun newBatchedServiceAreaBudget(code: String, title: String): BatchedServiceAreaBudget {
    return BatchedServiceAreaBudget(
        code = code,
        title = title,
        contactEmail = "",
        revision = 0,
        ownerId = "",
        threshold = 0.0,
        windowDays = 0,
        odometer = 0,
        status = BatchedServiceAreaBudgetStatus.DRAFT,
    )
}

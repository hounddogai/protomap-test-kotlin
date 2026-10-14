package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched capacity group is in its life cycle. */
enum class BatchedCapacityGroupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One batched capacity group, with what the planners need to act on it. */
data class BatchedCapacityGroup(
    var code: String,
    var label: String,
    var graceDays: Int,
    var notes: String,
    var dock: String,
    var unit: String,
    var amount: Double,
    var margin: Double,
    var routeCode: String,
    var endsAt: Int,
    var expiresAt: Int,
    var status: BatchedCapacityGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCapacityGroupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCapacityGroupStatus): Boolean {
        when (status) {
            BatchedCapacityGroupStatus.OPEN -> {
                when (next) {
                    BatchedCapacityGroupStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedCapacityGroupStatus.IN_REVIEW -> return next == BatchedCapacityGroupStatus.ARCHIVED
            BatchedCapacityGroupStatus.RESOLVED -> return next == BatchedCapacityGroupStatus.REOPENED || next == BatchedCapacityGroupStatus.ARCHIVED
            BatchedCapacityGroupStatus.REOPENED -> {
                when (next) {
                    BatchedCapacityGroupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedCapacityGroupStatus) {
        if (!canMoveTo(next)) {
            error("batched capacity group $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"batched capacity group"} $code"

    fun netTotal(): Double = round(amount * margin * 100) / 100

    fun snapshot(): BatchedCapacityGroup {
        return BatchedCapacityGroup(
            code = code,
            label = label,
            graceDays = graceDays,
            notes = notes,
            dock = dock,
            unit = unit,
            amount = amount,
            margin = margin,
            routeCode = routeCode,
            endsAt = endsAt,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a batched capacity group in status open. */
fun newBatchedCapacityGroup(code: String, label: String): BatchedCapacityGroup {
    return BatchedCapacityGroup(
        code = code,
        label = label,
        graceDays = 0,
        notes = "",
        dock = "",
        unit = "",
        amount = 0.0,
        margin = 0.0,
        routeCode = "",
        endsAt = 0,
        expiresAt = 0,
        status = BatchedCapacityGroupStatus.OPEN,
    )
}

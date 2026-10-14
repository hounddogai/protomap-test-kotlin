package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending sortation batch is in its life cycle. */
enum class PendingSortationBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One pending sortation batch, with what the planners need to act on it. */
data class PendingSortationBatch(
    var code: String,
    var name: String,
    var routeCode: String,
    var visible: Boolean,
    var ceiling: Double,
    var cost: Double,
    var minimum: Double,
    var priority: Int,
    var region: String,
    var shiftCode: String,
    var pinned: Boolean,
    var status: PendingSortationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingSortationBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingSortationBatchStatus): Boolean {
        when (status) {
            PendingSortationBatchStatus.DRAFT -> return next == PendingSortationBatchStatus.ACTIVE || next == PendingSortationBatchStatus.SUSPENDED
            PendingSortationBatchStatus.PENDING -> {
                when (next) {
                    PendingSortationBatchStatus.CLOSED -> return true
                }
                return false
            }
            PendingSortationBatchStatus.ACTIVE -> {
                when (next) {
                    PendingSortationBatchStatus.SUSPENDED -> return true
                }
                return false
            }
            PendingSortationBatchStatus.SUSPENDED -> return next == PendingSortationBatchStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PendingSortationBatchStatus) {
        if (!canMoveTo(next)) {
            error("pending sortation batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveExposure(): Double = round(ceiling * minimum * 100) / 100

    fun snapshot(): PendingSortationBatch {
        return PendingSortationBatch(
            code = code,
            name = name,
            routeCode = routeCode,
            visible = visible,
            ceiling = ceiling,
            cost = cost,
            minimum = minimum,
            priority = priority,
            region = region,
            shiftCode = shiftCode,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending sortation batch in status draft. */
fun newPendingSortationBatch(code: String, name: String): PendingSortationBatch {
    return PendingSortationBatch(
        code = code,
        name = name,
        routeCode = "",
        visible = false,
        ceiling = 0.0,
        cost = 0.0,
        minimum = 0.0,
        priority = 0,
        region = "",
        shiftCode = "",
        pinned = false,
        status = PendingSortationBatchStatus.DRAFT,
    )
}

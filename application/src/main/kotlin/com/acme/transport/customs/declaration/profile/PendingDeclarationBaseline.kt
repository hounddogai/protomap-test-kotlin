package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending declaration baseline is in its life cycle. */
enum class PendingDeclarationBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending declaration baseline, with what the planners need to act on it. */
data class PendingDeclarationBaseline(
    var code: String,
    var name: String,
    var batchSize: Int,
    var unit: String,
    var attempts: Int,
    var limit: Int,
    var pinned: Boolean,
    var status: PendingDeclarationBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDeclarationBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDeclarationBaselineStatus): Boolean {
        when (status) {
            PendingDeclarationBaselineStatus.PLANNED -> return next == PendingDeclarationBaselineStatus.IN_PROGRESS || next == PendingDeclarationBaselineStatus.COMPLETED
            PendingDeclarationBaselineStatus.SCHEDULED -> return next == PendingDeclarationBaselineStatus.IN_PROGRESS || next == PendingDeclarationBaselineStatus.CANCELLED
            PendingDeclarationBaselineStatus.IN_PROGRESS -> return next == PendingDeclarationBaselineStatus.COMPLETED
            PendingDeclarationBaselineStatus.COMPLETED -> {
                when (next) {
                    PendingDeclarationBaselineStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingDeclarationBaselineStatus) {
        if (!canMoveTo(next)) {
            error("pending declaration baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending declaration baseline"} $code"

    fun projectedVolume(): Double = round((attempts.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): PendingDeclarationBaseline {
        return PendingDeclarationBaseline(
            code = code,
            name = name,
            batchSize = batchSize,
            unit = unit,
            attempts = attempts,
            limit = limit,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending declaration baseline in status planned. */
fun newPendingDeclarationBaseline(code: String, name: String): PendingDeclarationBaseline {
    return PendingDeclarationBaseline(
        code = code,
        name = name,
        batchSize = 0,
        unit = "",
        attempts = 0,
        limit = 0,
        pinned = false,
        status = PendingDeclarationBaselineStatus.PLANNED,
    )
}

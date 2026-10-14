package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered camera event allocation is in its life cycle. */
enum class TieredCameraEventAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a tiered camera event allocation and its current status. */
data class TieredCameraEventAllocation(
    var code: String,
    var title: String,
    var sequence: Int,
    var contactEmail: String,
    var budget: Double,
    var status: TieredCameraEventAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCameraEventAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCameraEventAllocationStatus): Boolean {
        when (status) {
            TieredCameraEventAllocationStatus.DRAFT -> {
                when (next) {
                    TieredCameraEventAllocationStatus.SUSPENDED -> return true
                }
                return false
            }
            TieredCameraEventAllocationStatus.PENDING -> return next == TieredCameraEventAllocationStatus.ACTIVE
            TieredCameraEventAllocationStatus.ACTIVE -> return next == TieredCameraEventAllocationStatus.SUSPENDED || next == TieredCameraEventAllocationStatus.CLOSED
            TieredCameraEventAllocationStatus.SUSPENDED -> return next == TieredCameraEventAllocationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TieredCameraEventAllocationStatus) {
        if (!canMoveTo(next)) {
            error("tiered camera event allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedTotal(): Double = round((sequence.toDouble() + budget) * 100) / 100

    fun snapshot(): TieredCameraEventAllocation {
        return TieredCameraEventAllocation(
            code = code,
            title = title,
            sequence = sequence,
            contactEmail = contactEmail,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered camera event allocation in status draft. */
fun newTieredCameraEventAllocation(code: String, title: String): TieredCameraEventAllocation {
    return TieredCameraEventAllocation(
        code = code,
        title = title,
        sequence = 0,
        contactEmail = "",
        budget = 0.0,
        status = TieredCameraEventAllocationStatus.DRAFT,
    )
}

package com.acme.transport.lastmile.redelivery.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending redelivery slot is in its life cycle. */
enum class PendingRedeliverySlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One pending redelivery slot, with what the planners need to act on it. */
data class PendingRedeliverySlot(
    var code: String,
    var name: String,
    var batchSize: Int,
    var active: Boolean,
    var margin: Double,
    var status: PendingRedeliverySlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRedeliverySlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRedeliverySlotStatus): Boolean {
        when (status) {
            PendingRedeliverySlotStatus.PROPOSED -> {
                when (next) {
                    PendingRedeliverySlotStatus.APPROVED -> return true
                }
                return false
            }
            PendingRedeliverySlotStatus.APPROVED -> return next == PendingRedeliverySlotStatus.REJECTED || next == PendingRedeliverySlotStatus.WITHDRAWN
            PendingRedeliverySlotStatus.REJECTED -> return next == PendingRedeliverySlotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingRedeliverySlotStatus) {
        if (!canMoveTo(next)) {
            error("pending redelivery slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netTotal(): Double = round(margin * batchSize.toDouble() * 100) / 100

    fun snapshot(): PendingRedeliverySlot {
        return PendingRedeliverySlot(
            code = code,
            name = name,
            batchSize = batchSize,
            active = active,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending redelivery slot in status proposed. */
fun newPendingRedeliverySlot(code: String, name: String): PendingRedeliverySlot {
    return PendingRedeliverySlot(
        code = code,
        name = name,
        batchSize = 0,
        active = false,
        margin = 0.0,
        status = PendingRedeliverySlotStatus.PROPOSED,
    )
}

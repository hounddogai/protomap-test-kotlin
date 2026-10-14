package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending near miss slot is in its life cycle. */
enum class PendingNearMissSlotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending near miss slot. Its code is unique within a tenant. */
data class PendingNearMissSlot(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var unit: String,
    var budget: Double,
    var dock: String,
    var priority: Int,
    var pinned: Boolean,
    var title: String,
    var status: PendingNearMissSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingNearMissSlotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingNearMissSlotStatus): Boolean {
        when (status) {
            PendingNearMissSlotStatus.DRAFT -> {
                when (next) {
                    PendingNearMissSlotStatus.CLOSED -> return true
                }
                return false
            }
            PendingNearMissSlotStatus.PENDING -> {
                when (next) {
                    PendingNearMissSlotStatus.CLOSED -> return true
                }
                return false
            }
            PendingNearMissSlotStatus.ACTIVE -> {
                when (next) {
                    PendingNearMissSlotStatus.SUSPENDED, PendingNearMissSlotStatus.CLOSED -> return true
                }
                return false
            }
            PendingNearMissSlotStatus.SUSPENDED -> {
                when (next) {
                    PendingNearMissSlotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingNearMissSlotStatus) {
        if (!canMoveTo(next)) {
            error("pending near miss slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending near miss slot"} $code"

    fun adjustedValue(): Double = round((budget + priority.toDouble()) * 100) / 100

    fun snapshot(): PendingNearMissSlot {
        return PendingNearMissSlot(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            unit = unit,
            budget = budget,
            dock = dock,
            priority = priority,
            pinned = pinned,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending near miss slot in status draft. */
fun newPendingNearMissSlot(code: String, name: String): PendingNearMissSlot {
    return PendingNearMissSlot(
        code = code,
        name = name,
        etaMinutes = 0,
        unit = "",
        budget = 0.0,
        dock = "",
        priority = 0,
        pinned = false,
        title = "",
        status = PendingNearMissSlotStatus.DRAFT,
    )
}

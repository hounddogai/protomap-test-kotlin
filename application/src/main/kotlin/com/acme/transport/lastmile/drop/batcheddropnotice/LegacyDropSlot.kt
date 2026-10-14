package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.kit.text.normalize

/** Where a legacy drop slot is in its life cycle. */
enum class LegacyDropSlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a legacy drop slot and its current status. */
data class LegacyDropSlot(
    var code: String,
    var title: String,
    var ceiling: Double,
    var archived: Boolean,
    var country: String,
    var status: LegacyDropSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDropSlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDropSlotStatus): Boolean {
        when (status) {
            LegacyDropSlotStatus.PROPOSED -> {
                when (next) {
                    LegacyDropSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyDropSlotStatus.APPROVED -> return next == LegacyDropSlotStatus.REJECTED || next == LegacyDropSlotStatus.WITHDRAWN
            LegacyDropSlotStatus.REJECTED -> return next == LegacyDropSlotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyDropSlotStatus) {
        if (!canMoveTo(next)) {
            error("legacy drop slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = ceiling

    fun snapshot(): LegacyDropSlot {
        return LegacyDropSlot(
            code = code,
            title = title,
            ceiling = ceiling,
            archived = archived,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy drop slot in status proposed. */
fun newLegacyDropSlot(code: String, title: String): LegacyDropSlot {
    return LegacyDropSlot(
        code = code,
        title = title,
        ceiling = 0.0,
        archived = false,
        country = "",
        status = LegacyDropSlotStatus.PROPOSED,
    )
}

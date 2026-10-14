package com.acme.transport.crewing.swap.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered swap slot records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a tiered swap slot is in its life cycle. */
enum class TieredSwapSlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tiered swap slot as the swap team keeps it. */
data class TieredSwapSlot(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var description: String,
    var capacity: Int,
    var tags: MutableList<String>,
    var status: TieredSwapSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredSwapSlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredSwapSlotStatus): Boolean {
        when (status) {
            TieredSwapSlotStatus.REQUESTED -> return next == TieredSwapSlotStatus.EXPIRED
            TieredSwapSlotStatus.GRANTED -> return next == TieredSwapSlotStatus.REVOKED || next == TieredSwapSlotStatus.EXPIRED
            TieredSwapSlotStatus.REVOKED -> {
                when (next) {
                    TieredSwapSlotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredSwapSlotStatus) {
        if (!canMoveTo(next)) {
            error("tiered swap slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round((capacity.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): TieredSwapSlot {
        return TieredSwapSlot(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            description = description,
            capacity = capacity,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered swap slot in status requested. */
fun newTieredSwapSlot(code: String, label: String): TieredSwapSlot {
    return TieredSwapSlot(
        code = code,
        label = label,
        durationMinutes = 0,
        description = "",
        capacity = 0,
        tags = mutableListOf<String>(),
        status = TieredSwapSlotStatus.REQUESTED,
    )
}

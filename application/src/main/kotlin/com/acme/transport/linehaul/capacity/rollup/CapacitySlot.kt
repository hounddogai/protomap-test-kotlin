package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity slot is in its life cycle. */
enum class CapacitySlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capacity slot. Its code is unique within a tenant. */
data class CapacitySlot(
    var code: String,
    var label: String,
    var endsAt: Int,
    var pinned: Boolean,
    var rate: Double,
    var limit: Int,
    var dock: String,
    var status: CapacitySlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacitySlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacitySlotStatus): Boolean {
        when (status) {
            CapacitySlotStatus.PROPOSED -> {
                when (next) {
                    CapacitySlotStatus.APPROVED, CapacitySlotStatus.WITHDRAWN -> return true
                }
                return false
            }
            CapacitySlotStatus.APPROVED -> {
                when (next) {
                    CapacitySlotStatus.REJECTED -> return true
                }
                return false
            }
            CapacitySlotStatus.REJECTED -> return next == CapacitySlotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CapacitySlotStatus) {
        if (!canMoveTo(next)) {
            error("capacity slot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedValue(): Double = round(rate * limit.toDouble() * 100) / 100

    fun snapshot(): CapacitySlot {
        return CapacitySlot(
            code = code,
            label = label,
            endsAt = endsAt,
            pinned = pinned,
            rate = rate,
            limit = limit,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capacity slot in status proposed. */
fun newCapacitySlot(code: String, label: String): CapacitySlot {
    return CapacitySlot(
        code = code,
        label = label,
        endsAt = 0,
        pinned = false,
        rate = 0.0,
        limit = 0,
        dock = "",
        status = CapacitySlotStatus.PROPOSED,
    )
}

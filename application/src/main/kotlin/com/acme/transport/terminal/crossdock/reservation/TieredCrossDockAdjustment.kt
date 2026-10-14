package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.kit.text.normalize

/** Where a tiered cross dock adjustment is in its life cycle. */
enum class TieredCrossDockAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered cross dock adjustment as the cross dock team keeps it. */
data class TieredCrossDockAdjustment(
    var code: String,
    var name: String,
    var notes: String,
    var active: Boolean,
    var minimum: Double,
    var status: TieredCrossDockAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCrossDockAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCrossDockAdjustmentStatus): Boolean {
        when (status) {
            TieredCrossDockAdjustmentStatus.PROPOSED -> {
                when (next) {
                    TieredCrossDockAdjustmentStatus.REJECTED, TieredCrossDockAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredCrossDockAdjustmentStatus.APPROVED -> return next == TieredCrossDockAdjustmentStatus.REJECTED
            TieredCrossDockAdjustmentStatus.REJECTED -> {
                when (next) {
                    TieredCrossDockAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredCrossDockAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("tiered cross dock adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = minimum

    fun snapshot(): TieredCrossDockAdjustment {
        return TieredCrossDockAdjustment(
            code = code,
            name = name,
            notes = notes,
            active = active,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered cross dock adjustment in status proposed. */
fun newTieredCrossDockAdjustment(code: String, name: String): TieredCrossDockAdjustment {
    return TieredCrossDockAdjustment(
        code = code,
        name = name,
        notes = "",
        active = false,
        minimum = 0.0,
        status = TieredCrossDockAdjustmentStatus.PROPOSED,
    )
}

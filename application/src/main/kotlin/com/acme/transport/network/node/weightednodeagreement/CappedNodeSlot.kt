package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped node slot is in its life cycle. */
enum class CappedNodeSlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped node slot as the node team keeps it. */
data class CappedNodeSlot(
    var code: String,
    var name: String,
    var label: String,
    var ratio: Double,
    var graceDays: Int,
    var lane: String,
    var status: CappedNodeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedNodeSlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedNodeSlotStatus): Boolean {
        when (status) {
            CappedNodeSlotStatus.PROPOSED -> {
                when (next) {
                    CappedNodeSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedNodeSlotStatus.APPROVED -> return next == CappedNodeSlotStatus.REJECTED || next == CappedNodeSlotStatus.WITHDRAWN
            CappedNodeSlotStatus.REJECTED -> {
                when (next) {
                    CappedNodeSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedNodeSlotStatus) {
        if (!canMoveTo(next)) {
            error("capped node slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $name"

    fun adjustedExposure(): Double = round(ratio * graceDays.toDouble() * 100) / 100

    fun snapshot(): CappedNodeSlot {
        return CappedNodeSlot(
            code = code,
            name = name,
            label = label,
            ratio = ratio,
            graceDays = graceDays,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capped node slot in status proposed. */
fun newCappedNodeSlot(code: String, name: String): CappedNodeSlot {
    return CappedNodeSlot(
        code = code,
        name = name,
        label = "",
        ratio = 0.0,
        graceDays = 0,
        lane = "",
        status = CappedNodeSlotStatus.PROPOSED,
    )
}

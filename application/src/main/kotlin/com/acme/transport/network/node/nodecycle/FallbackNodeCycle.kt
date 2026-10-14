package com.acme.transport.network.node.nodecycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback node cycle records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a fallback node cycle is in its life cycle. */
enum class FallbackNodeCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fallback node cycle, with what the planners need to act on it. */
data class FallbackNodeCycle(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var shiftCode: String,
    var floorValue: Double,
    var vehicleId: String,
    var ratio: Double,
    var status: FallbackNodeCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackNodeCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackNodeCycleStatus): Boolean {
        when (status) {
            FallbackNodeCycleStatus.DRAFT -> return next == FallbackNodeCycleStatus.PENDING || next == FallbackNodeCycleStatus.ACTIVE
            FallbackNodeCycleStatus.PENDING -> {
                when (next) {
                    FallbackNodeCycleStatus.ACTIVE, FallbackNodeCycleStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackNodeCycleStatus.ACTIVE -> return next == FallbackNodeCycleStatus.SUSPENDED || next == FallbackNodeCycleStatus.CLOSED
            FallbackNodeCycleStatus.SUSPENDED -> {
                when (next) {
                    FallbackNodeCycleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackNodeCycleStatus) {
        if (!canMoveTo(next)) {
            error("fallback node cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round((ratio + floorValue) * 100) / 100

    fun snapshot(): FallbackNodeCycle {
        return FallbackNodeCycle(
            code = code,
            name = name,
            pinned = pinned,
            shiftCode = shiftCode,
            floorValue = floorValue,
            vehicleId = vehicleId,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback node cycle in status draft. */
fun newFallbackNodeCycle(code: String, name: String): FallbackNodeCycle {
    return FallbackNodeCycle(
        code = code,
        name = name,
        pinned = false,
        shiftCode = "",
        floorValue = 0.0,
        vehicleId = "",
        ratio = 0.0,
        status = FallbackNodeCycleStatus.DRAFT,
    )
}

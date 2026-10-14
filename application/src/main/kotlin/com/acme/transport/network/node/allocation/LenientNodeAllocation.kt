package com.acme.transport.network.node.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient node allocation records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a lenient node allocation is in its life cycle. */
enum class LenientNodeAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a lenient node allocation and its current status. */
data class LenientNodeAllocation(
    var code: String,
    var label: String,
    var limit: Int,
    var minimum: Double,
    var priority: Int,
    var floorValue: Double,
    var attributes: MutableMap<String, String>,
    var expiresAt: Int,
    var status: LenientNodeAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientNodeAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientNodeAllocationStatus): Boolean {
        when (status) {
            LenientNodeAllocationStatus.PLANNED -> return next == LenientNodeAllocationStatus.IN_PROGRESS
            LenientNodeAllocationStatus.SCHEDULED -> {
                when (next) {
                    LenientNodeAllocationStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LenientNodeAllocationStatus.IN_PROGRESS -> {
                when (next) {
                    LenientNodeAllocationStatus.COMPLETED, LenientNodeAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            LenientNodeAllocationStatus.COMPLETED -> {
                when (next) {
                    LenientNodeAllocationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientNodeAllocationStatus) {
        if (!canMoveTo(next)) {
            error("lenient node allocation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"lenient node allocation"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round(expiresAt.toDouble() * minimum * 100) / 100

    fun snapshot(): LenientNodeAllocation {
        return LenientNodeAllocation(
            code = code,
            label = label,
            limit = limit,
            minimum = minimum,
            priority = priority,
            floorValue = floorValue,
            attributes = attributes,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient node allocation in status planned. */
fun newLenientNodeAllocation(code: String, label: String): LenientNodeAllocation {
    return LenientNodeAllocation(
        code = code,
        label = label,
        limit = 0,
        minimum = 0.0,
        priority = 0,
        floorValue = 0.0,
        attributes = mutableMapOf<String, String>(),
        expiresAt = 0,
        status = LenientNodeAllocationStatus.PLANNED,
    )
}

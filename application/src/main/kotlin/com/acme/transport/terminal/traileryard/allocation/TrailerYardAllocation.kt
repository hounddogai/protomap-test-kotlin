package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer yard allocation records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a trailer yard allocation is in its life cycle. */
enum class TrailerYardAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A trailer yard allocation. Its code is unique within a tenant. */
data class TrailerYardAllocation(
    var code: String,
    var label: String,
    var archived: Boolean,
    var segment: String,
    var attempts: Int,
    var durationMinutes: Int,
    var priority: Int,
    var graceDays: Int,
    var status: TrailerYardAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardAllocationStatus): Boolean {
        when (status) {
            TrailerYardAllocationStatus.IDLE -> {
                when (next) {
                    TrailerYardAllocationStatus.READY -> return true
                }
                return false
            }
            TrailerYardAllocationStatus.WARMING -> {
                when (next) {
                    TrailerYardAllocationStatus.READY, TrailerYardAllocationStatus.STOPPED -> return true
                }
                return false
            }
            TrailerYardAllocationStatus.READY -> {
                when (next) {
                    TrailerYardAllocationStatus.DRAINING -> return true
                }
                return false
            }
            TrailerYardAllocationStatus.DRAINING -> return next == TrailerYardAllocationStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TrailerYardAllocationStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedExposure(): Double = round(priority.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): TrailerYardAllocation {
        return TrailerYardAllocation(
            code = code,
            label = label,
            archived = archived,
            segment = segment,
            attempts = attempts,
            durationMinutes = durationMinutes,
            priority = priority,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a trailer yard allocation in status idle. */
fun newTrailerYardAllocation(code: String, label: String): TrailerYardAllocation {
    return TrailerYardAllocation(
        code = code,
        label = label,
        archived = false,
        segment = "",
        attempts = 0,
        durationMinutes = 0,
        priority = 0,
        graceDays = 0,
        status = TrailerYardAllocationStatus.IDLE,
    )
}

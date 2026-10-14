package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.text.normalize

/** Where a trailer yard cycle is in its life cycle. */
enum class TrailerYardCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A trailer yard cycle. Its code is unique within a tenant. */
data class TrailerYardCycle(
    var code: String,
    var title: String,
    var timezone: String,
    var currency: String,
    var pinned: Boolean,
    var ownerId: String,
    var priority: Int,
    var status: TrailerYardCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardCycleStatus): Boolean {
        when (status) {
            TrailerYardCycleStatus.IDLE -> {
                when (next) {
                    TrailerYardCycleStatus.WARMING -> return true
                }
                return false
            }
            TrailerYardCycleStatus.WARMING -> return next == TrailerYardCycleStatus.DRAINING || next == TrailerYardCycleStatus.STOPPED
            TrailerYardCycleStatus.READY -> return next == TrailerYardCycleStatus.DRAINING || next == TrailerYardCycleStatus.STOPPED
            TrailerYardCycleStatus.DRAINING -> {
                when (next) {
                    TrailerYardCycleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardCycleStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): TrailerYardCycle {
        return TrailerYardCycle(
            code = code,
            title = title,
            timezone = timezone,
            currency = currency,
            pinned = pinned,
            ownerId = ownerId,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a trailer yard cycle in status idle. */
fun newTrailerYardCycle(code: String, title: String): TrailerYardCycle {
    return TrailerYardCycle(
        code = code,
        title = title,
        timezone = "",
        currency = "",
        pinned = false,
        ownerId = "",
        priority = 0,
        status = TrailerYardCycleStatus.IDLE,
    )
}

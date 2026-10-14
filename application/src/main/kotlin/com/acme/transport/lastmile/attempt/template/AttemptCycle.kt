package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt cycle is in its life cycle. */
enum class AttemptCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A attempt cycle. Its code is unique within a tenant. */
data class AttemptCycle(
    var code: String,
    var name: String,
    var capacity: Int,
    var depot: String,
    var odometer: Int,
    var notes: String,
    var sequence: Int,
    var dock: String,
    var windowDays: Int,
    var archived: Boolean,
    var lane: String,
    var status: AttemptCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptCycleStatus): Boolean {
        when (status) {
            AttemptCycleStatus.OPEN -> {
                when (next) {
                    AttemptCycleStatus.RESOLVED -> return true
                }
                return false
            }
            AttemptCycleStatus.IN_REVIEW -> return next == AttemptCycleStatus.REOPENED || next == AttemptCycleStatus.ARCHIVED
            AttemptCycleStatus.RESOLVED -> return next == AttemptCycleStatus.REOPENED
            AttemptCycleStatus.REOPENED -> return next == AttemptCycleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AttemptCycleStatus) {
        if (!canMoveTo(next)) {
            error("attempt cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"attempt cycle"} $code"

    fun effectiveValue(): Double = round((sequence.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): AttemptCycle {
        return AttemptCycle(
            code = code,
            name = name,
            capacity = capacity,
            depot = depot,
            odometer = odometer,
            notes = notes,
            sequence = sequence,
            dock = dock,
            windowDays = windowDays,
            archived = archived,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a attempt cycle in status open. */
fun newAttemptCycle(code: String, name: String): AttemptCycle {
    return AttemptCycle(
        code = code,
        name = name,
        capacity = 0,
        depot = "",
        odometer = 0,
        notes = "",
        sequence = 0,
        dock = "",
        windowDays = 0,
        archived = false,
        lane = "",
        status = AttemptCycleStatus.OPEN,
    )
}

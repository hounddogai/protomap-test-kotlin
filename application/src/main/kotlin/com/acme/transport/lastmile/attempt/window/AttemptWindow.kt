package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.text.normalize

// The attempt window records of lastmile's attempt work, and the rules, stores, and calculations around them.

/** Where a attempt window is in its life cycle. */
enum class AttemptWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One attempt window, with what the planners need to act on it. */
data class AttemptWindow(
    var code: String,
    var label: String,
    var segment: String,
    var notes: String,
    var pinned: Boolean,
    var title: String,
    var amount: Double,
    var status: AttemptWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptWindowStatus): Boolean {
        when (status) {
            AttemptWindowStatus.IDLE -> return next == AttemptWindowStatus.READY
            AttemptWindowStatus.WARMING -> return next == AttemptWindowStatus.READY || next == AttemptWindowStatus.DRAINING
            AttemptWindowStatus.READY -> return next == AttemptWindowStatus.DRAINING || next == AttemptWindowStatus.STOPPED
            AttemptWindowStatus.DRAINING -> return next == AttemptWindowStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AttemptWindowStatus) {
        if (!canMoveTo(next)) {
            error("attempt window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"attempt window"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): AttemptWindow {
        return AttemptWindow(
            code = code,
            label = label,
            segment = segment,
            notes = notes,
            pinned = pinned,
            title = title,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a attempt window in status idle. */
fun newAttemptWindow(code: String, label: String): AttemptWindow {
    return AttemptWindow(
        code = code,
        label = label,
        segment = "",
        notes = "",
        pinned = false,
        title = "",
        amount = 0.0,
        status = AttemptWindowStatus.IDLE,
    )
}

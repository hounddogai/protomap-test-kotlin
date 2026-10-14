package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.kit.text.normalize

// The pending cutoff segment records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a pending cutoff segment is in its life cycle. */
enum class PendingCutoffSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a pending cutoff segment and its current status. */
data class PendingCutoffSegment(
    var code: String,
    var name: String,
    var locale: String,
    var segment: String,
    var label: String,
    var description: String,
    var carrier: String,
    var budget: Double,
    var status: PendingCutoffSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCutoffSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCutoffSegmentStatus): Boolean {
        when (status) {
            PendingCutoffSegmentStatus.PROPOSED -> {
                when (next) {
                    PendingCutoffSegmentStatus.REJECTED -> return true
                }
                return false
            }
            PendingCutoffSegmentStatus.APPROVED -> {
                when (next) {
                    PendingCutoffSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingCutoffSegmentStatus.REJECTED -> return next == PendingCutoffSegmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingCutoffSegmentStatus) {
        if (!canMoveTo(next)) {
            error("pending cutoff segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending cutoff segment"} $code"

    fun measuredValue(): Double = budget

    fun snapshot(): PendingCutoffSegment {
        return PendingCutoffSegment(
            code = code,
            name = name,
            locale = locale,
            segment = segment,
            label = label,
            description = description,
            carrier = carrier,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a pending cutoff segment in status proposed. */
fun newPendingCutoffSegment(code: String, name: String): PendingCutoffSegment {
    return PendingCutoffSegment(
        code = code,
        name = name,
        locale = "",
        segment = "",
        label = "",
        description = "",
        carrier = "",
        budget = 0.0,
        status = PendingCutoffSegmentStatus.PROPOSED,
    )
}

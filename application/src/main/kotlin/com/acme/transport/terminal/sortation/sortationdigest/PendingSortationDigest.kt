package com.acme.transport.terminal.sortation.sortationdigest

import com.acme.transport.kit.text.normalize

// The pending sortation digest records of terminal's sortation work, and the rules, stores, and calculations
// around them.

/** Where a pending sortation digest is in its life cycle. */
enum class PendingSortationDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One pending sortation digest, with what the planners need to act on it. */
data class PendingSortationDigest(
    var code: String,
    var label: String,
    var unit: String,
    var locale: String,
    var graceDays: Int,
    var status: PendingSortationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingSortationDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingSortationDigestStatus): Boolean {
        when (status) {
            PendingSortationDigestStatus.DRAFT -> return next == PendingSortationDigestStatus.ACTIVE || next == PendingSortationDigestStatus.CLOSED
            PendingSortationDigestStatus.PENDING -> return next == PendingSortationDigestStatus.ACTIVE || next == PendingSortationDigestStatus.SUSPENDED
            PendingSortationDigestStatus.ACTIVE -> {
                when (next) {
                    PendingSortationDigestStatus.CLOSED -> return true
                }
                return false
            }
            PendingSortationDigestStatus.SUSPENDED -> {
                when (next) {
                    PendingSortationDigestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingSortationDigestStatus) {
        if (!canMoveTo(next)) {
            error("pending sortation digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): PendingSortationDigest {
        return PendingSortationDigest(
            code = code,
            label = label,
            unit = unit,
            locale = locale,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending sortation digest in status draft. */
fun newPendingSortationDigest(code: String, label: String): PendingSortationDigest {
    return PendingSortationDigest(
        code = code,
        label = label,
        unit = "",
        locale = "",
        graceDays = 0,
        status = PendingSortationDigestStatus.DRAFT,
    )
}

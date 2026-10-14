package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.kit.text.normalize

/** Where a capped dock door baseline is in its life cycle. */
enum class CappedDockDoorBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a capped dock door baseline and its current status. */
data class CappedDockDoorBaseline(
    var code: String,
    var title: String,
    var currency: String,
    var summary: String,
    var description: String,
    var segment: String,
    var amount: Double,
    var status: CappedDockDoorBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDockDoorBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDockDoorBaselineStatus): Boolean {
        when (status) {
            CappedDockDoorBaselineStatus.PLANNED -> return next == CappedDockDoorBaselineStatus.SCHEDULED || next == CappedDockDoorBaselineStatus.IN_PROGRESS
            CappedDockDoorBaselineStatus.SCHEDULED -> return next == CappedDockDoorBaselineStatus.CANCELLED
            CappedDockDoorBaselineStatus.IN_PROGRESS -> return next == CappedDockDoorBaselineStatus.CANCELLED
            CappedDockDoorBaselineStatus.COMPLETED -> return next == CappedDockDoorBaselineStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CappedDockDoorBaselineStatus) {
        if (!canMoveTo(next)) {
            error("capped dock door baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped dock door baseline"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): CappedDockDoorBaseline {
        return CappedDockDoorBaseline(
            code = code,
            title = title,
            currency = currency,
            summary = summary,
            description = description,
            segment = segment,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capped dock door baseline in status planned. */
fun newCappedDockDoorBaseline(code: String, title: String): CappedDockDoorBaseline {
    return CappedDockDoorBaseline(
        code = code,
        title = title,
        currency = "",
        summary = "",
        description = "",
        segment = "",
        amount = 0.0,
        status = CappedDockDoorBaselineStatus.PLANNED,
    )
}

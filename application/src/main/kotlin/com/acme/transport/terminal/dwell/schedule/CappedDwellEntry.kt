package com.acme.transport.terminal.dwell.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped dwell entry is in its life cycle. */
enum class CappedDwellEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped dwell entry as the dwell team keeps it. */
data class CappedDwellEntry(
    var code: String,
    var title: String,
    var cost: Double,
    var reviewer: String,
    var updatedAt: Int,
    var endsAt: Int,
    var threshold: Double,
    var budget: Double,
    var status: CappedDwellEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDwellEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDwellEntryStatus): Boolean {
        when (status) {
            CappedDwellEntryStatus.DRAFT -> {
                when (next) {
                    CappedDwellEntryStatus.PENDING, CappedDwellEntryStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedDwellEntryStatus.PENDING -> return next == CappedDwellEntryStatus.ACTIVE
            CappedDwellEntryStatus.ACTIVE -> return next == CappedDwellEntryStatus.SUSPENDED || next == CappedDwellEntryStatus.CLOSED
            CappedDwellEntryStatus.SUSPENDED -> return next == CappedDwellEntryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedDwellEntryStatus) {
        if (!canMoveTo(next)) {
            error("capped dwell entry $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped dwell entry"} $code"

    fun adjustedLoad(): Double = round(threshold * budget * 100) / 100

    fun snapshot(): CappedDwellEntry {
        return CappedDwellEntry(
            code = code,
            title = title,
            cost = cost,
            reviewer = reviewer,
            updatedAt = updatedAt,
            endsAt = endsAt,
            threshold = threshold,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped dwell entry in status draft. */
fun newCappedDwellEntry(code: String, title: String): CappedDwellEntry {
    return CappedDwellEntry(
        code = code,
        title = title,
        cost = 0.0,
        reviewer = "",
        updatedAt = 0,
        endsAt = 0,
        threshold = 0.0,
        budget = 0.0,
        status = CappedDwellEntryStatus.DRAFT,
    )
}

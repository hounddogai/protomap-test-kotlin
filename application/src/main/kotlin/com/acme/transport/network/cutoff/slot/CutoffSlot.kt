package com.acme.transport.network.cutoff.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cutoff slot records of network's cutoff work, and the rules, stores, and calculations around them.

/** Where a cutoff slot is in its life cycle. */
enum class CutoffSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a cutoff slot and its current status. */
data class CutoffSlot(
    var code: String,
    var title: String,
    var endsAt: Int,
    var approved: Boolean,
    var trailerId: String,
    var retries: Int,
    var reviewer: String,
    var priority: Int,
    var archived: Boolean,
    var status: CutoffSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffSlotStatus): Boolean {
        when (status) {
            CutoffSlotStatus.OPEN -> {
                when (next) {
                    CutoffSlotStatus.REOPENED -> return true
                }
                return false
            }
            CutoffSlotStatus.IN_REVIEW -> {
                when (next) {
                    CutoffSlotStatus.REOPENED -> return true
                }
                return false
            }
            CutoffSlotStatus.RESOLVED -> return next == CutoffSlotStatus.REOPENED || next == CutoffSlotStatus.ARCHIVED
            CutoffSlotStatus.REOPENED -> return next == CutoffSlotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CutoffSlotStatus) {
        if (!canMoveTo(next)) {
            error("cutoff slot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedTotal(): Double = round(endsAt.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): CutoffSlot {
        return CutoffSlot(
            code = code,
            title = title,
            endsAt = endsAt,
            approved = approved,
            trailerId = trailerId,
            retries = retries,
            reviewer = reviewer,
            priority = priority,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cutoff slot in status open. */
fun newCutoffSlot(code: String, title: String): CutoffSlot {
    return CutoffSlot(
        code = code,
        title = title,
        endsAt = 0,
        approved = false,
        trailerId = "",
        retries = 0,
        reviewer = "",
        priority = 0,
        archived = false,
        status = CutoffSlotStatus.OPEN,
    )
}

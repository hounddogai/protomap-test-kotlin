package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered roster milestone records of crewing's roster work, and the rules, stores, and calculations
// around them.

/** Where a tiered roster milestone is in its life cycle. */
enum class TieredRosterMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tiered roster milestone and its current status. */
data class TieredRosterMilestone(
    var code: String,
    var title: String,
    var notes: String,
    var sequence: Int,
    var expiresAt: Int,
    var score: Double,
    var quantity: Int,
    var status: TieredRosterMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRosterMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRosterMilestoneStatus): Boolean {
        when (status) {
            TieredRosterMilestoneStatus.NEW -> return next == TieredRosterMilestoneStatus.DONE || next == TieredRosterMilestoneStatus.FAILED
            TieredRosterMilestoneStatus.QUEUED -> return next == TieredRosterMilestoneStatus.RUNNING
            TieredRosterMilestoneStatus.RUNNING -> {
                when (next) {
                    TieredRosterMilestoneStatus.DONE, TieredRosterMilestoneStatus.FAILED -> return true
                }
                return false
            }
            TieredRosterMilestoneStatus.DONE -> {
                when (next) {
                    TieredRosterMilestoneStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredRosterMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("tiered roster milestone $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round(quantity.toDouble() * score * 100) / 100

    fun snapshot(): TieredRosterMilestone {
        return TieredRosterMilestone(
            code = code,
            title = title,
            notes = notes,
            sequence = sequence,
            expiresAt = expiresAt,
            score = score,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered roster milestone in status new. */
fun newTieredRosterMilestone(code: String, title: String): TieredRosterMilestone {
    return TieredRosterMilestone(
        code = code,
        title = title,
        notes = "",
        sequence = 0,
        expiresAt = 0,
        score = 0.0,
        quantity = 0,
        status = TieredRosterMilestoneStatus.NEW,
    )
}

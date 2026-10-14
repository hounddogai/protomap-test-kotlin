package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary sortation milestone records of terminal's sortation work, and the rules, stores, and
// calculations around them.

/** Where a primary sortation milestone is in its life cycle. */
enum class PrimarySortationMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A primary sortation milestone as the sortation team keeps it. */
data class PrimarySortationMilestone(
    var code: String,
    var name: String,
    var unit: String,
    var revision: Int,
    var priority: Int,
    var segment: String,
    var status: PrimarySortationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimarySortationMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimarySortationMilestoneStatus): Boolean {
        when (status) {
            PrimarySortationMilestoneStatus.DRAFT -> {
                when (next) {
                    PrimarySortationMilestoneStatus.ACTIVE, PrimarySortationMilestoneStatus.CLOSED -> return true
                }
                return false
            }
            PrimarySortationMilestoneStatus.PENDING -> {
                when (next) {
                    PrimarySortationMilestoneStatus.ACTIVE, PrimarySortationMilestoneStatus.SUSPENDED -> return true
                }
                return false
            }
            PrimarySortationMilestoneStatus.ACTIVE -> {
                when (next) {
                    PrimarySortationMilestoneStatus.SUSPENDED, PrimarySortationMilestoneStatus.CLOSED -> return true
                }
                return false
            }
            PrimarySortationMilestoneStatus.SUSPENDED -> {
                when (next) {
                    PrimarySortationMilestoneStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimarySortationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("primary sortation milestone $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun adjustedLoad(): Double = round((revision.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): PrimarySortationMilestone {
        return PrimarySortationMilestone(
            code = code,
            name = name,
            unit = unit,
            revision = revision,
            priority = priority,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a primary sortation milestone in status draft. */
fun newPrimarySortationMilestone(code: String, name: String): PrimarySortationMilestone {
    return PrimarySortationMilestone(
        code = code,
        name = name,
        unit = "",
        revision = 0,
        priority = 0,
        segment = "",
        status = PrimarySortationMilestoneStatus.DRAFT,
    )
}

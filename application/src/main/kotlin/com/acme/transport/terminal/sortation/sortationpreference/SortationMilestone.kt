package com.acme.transport.terminal.sortation.sortationpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation milestone is in its life cycle. */
enum class SortationMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One sortation milestone, with what the planners need to act on it. */
data class SortationMilestone(
    var code: String,
    var label: String,
    var currency: String,
    var archived: Boolean,
    var margin: Double,
    var description: String,
    var revision: Int,
    var maximum: Double,
    var status: SortationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationMilestoneStatus): Boolean {
        when (status) {
            SortationMilestoneStatus.DRAFT -> {
                when (next) {
                    SortationMilestoneStatus.PENDING -> return true
                }
                return false
            }
            SortationMilestoneStatus.PENDING -> {
                when (next) {
                    SortationMilestoneStatus.ACTIVE, SortationMilestoneStatus.CLOSED -> return true
                }
                return false
            }
            SortationMilestoneStatus.ACTIVE -> return next == SortationMilestoneStatus.SUSPENDED
            SortationMilestoneStatus.SUSPENDED -> return next == SortationMilestoneStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SortationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("sortation milestone $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"sortation milestone"} $code"

    fun weightedValue(): Double = round((maximum + revision.toDouble()) * 100) / 100

    fun snapshot(): SortationMilestone {
        return SortationMilestone(
            code = code,
            label = label,
            currency = currency,
            archived = archived,
            margin = margin,
            description = description,
            revision = revision,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a sortation milestone in status draft. */
fun newSortationMilestone(code: String, label: String): SortationMilestone {
    return SortationMilestone(
        code = code,
        label = label,
        currency = "",
        archived = false,
        margin = 0.0,
        description = "",
        revision = 0,
        maximum = 0.0,
        status = SortationMilestoneStatus.DRAFT,
    )
}

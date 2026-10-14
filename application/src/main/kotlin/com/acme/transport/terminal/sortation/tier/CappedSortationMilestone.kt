package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.text.normalize

/** Where a capped sortation milestone is in its life cycle. */
enum class CappedSortationMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One capped sortation milestone, with what the planners need to act on it. */
data class CappedSortationMilestone(
    var code: String,
    var label: String,
    var ownerId: String,
    var pinned: Boolean,
    var graceDays: Int,
    var status: CappedSortationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedSortationMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedSortationMilestoneStatus): Boolean {
        when (status) {
            CappedSortationMilestoneStatus.CREATED -> {
                when (next) {
                    CappedSortationMilestoneStatus.VALIDATED, CappedSortationMilestoneStatus.PUBLISHED -> return true
                }
                return false
            }
            CappedSortationMilestoneStatus.VALIDATED -> {
                when (next) {
                    CappedSortationMilestoneStatus.PUBLISHED, CappedSortationMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            CappedSortationMilestoneStatus.PUBLISHED -> {
                when (next) {
                    CappedSortationMilestoneStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedSortationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("capped sortation milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): CappedSortationMilestone {
        return CappedSortationMilestone(
            code = code,
            label = label,
            ownerId = ownerId,
            pinned = pinned,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped sortation milestone in status created. */
fun newCappedSortationMilestone(code: String, label: String): CappedSortationMilestone {
    return CappedSortationMilestone(
        code = code,
        label = label,
        ownerId = "",
        pinned = false,
        graceDays = 0,
        status = CappedSortationMilestoneStatus.CREATED,
    )
}

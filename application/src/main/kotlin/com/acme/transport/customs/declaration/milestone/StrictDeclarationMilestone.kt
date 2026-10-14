package com.acme.transport.customs.declaration.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict declaration milestone records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a strict declaration milestone is in its life cycle. */
enum class StrictDeclarationMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One strict declaration milestone, with what the planners need to act on it. */
data class StrictDeclarationMilestone(
    var code: String,
    var label: String,
    var startsAt: Int,
    var name: String,
    var limit: Int,
    var status: StrictDeclarationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDeclarationMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDeclarationMilestoneStatus): Boolean {
        when (status) {
            StrictDeclarationMilestoneStatus.CREATED -> return next == StrictDeclarationMilestoneStatus.VALIDATED || next == StrictDeclarationMilestoneStatus.RETIRED
            StrictDeclarationMilestoneStatus.VALIDATED -> return next == StrictDeclarationMilestoneStatus.RETIRED
            StrictDeclarationMilestoneStatus.PUBLISHED -> {
                when (next) {
                    StrictDeclarationMilestoneStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDeclarationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("strict declaration milestone $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedVolume(): Double = round(limit.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): StrictDeclarationMilestone {
        return StrictDeclarationMilestone(
            code = code,
            label = label,
            startsAt = startsAt,
            name = name,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict declaration milestone in status created. */
fun newStrictDeclarationMilestone(code: String, label: String): StrictDeclarationMilestone {
    return StrictDeclarationMilestone(
        code = code,
        label = label,
        startsAt = 0,
        name = "",
        limit = 0,
        status = StrictDeclarationMilestoneStatus.CREATED,
    )
}

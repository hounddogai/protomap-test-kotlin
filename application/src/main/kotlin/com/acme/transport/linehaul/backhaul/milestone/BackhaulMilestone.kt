package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The backhaul milestone records of linehaul's backhaul work, and the rules, stores, and calculations around
// them.

/** Where a backhaul milestone is in its life cycle. */
enum class BackhaulMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A backhaul milestone as the backhaul team keeps it. */
data class BackhaulMilestone(
    var code: String,
    var label: String,
    var endsAt: Int,
    var dock: String,
    var batchSize: Int,
    var depot: String,
    var status: BackhaulMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulMilestoneStatus): Boolean {
        when (status) {
            BackhaulMilestoneStatus.OPEN -> return next == BackhaulMilestoneStatus.IN_REVIEW
            BackhaulMilestoneStatus.IN_REVIEW -> {
                when (next) {
                    BackhaulMilestoneStatus.RESOLVED -> return true
                }
                return false
            }
            BackhaulMilestoneStatus.RESOLVED -> return next == BackhaulMilestoneStatus.REOPENED || next == BackhaulMilestoneStatus.ARCHIVED
            BackhaulMilestoneStatus.REOPENED -> return next == BackhaulMilestoneStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BackhaulMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("backhaul milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round((endsAt.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): BackhaulMilestone {
        return BackhaulMilestone(
            code = code,
            label = label,
            endsAt = endsAt,
            dock = dock,
            batchSize = batchSize,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a backhaul milestone in status open. */
fun newBackhaulMilestone(code: String, label: String): BackhaulMilestone {
    return BackhaulMilestone(
        code = code,
        label = label,
        endsAt = 0,
        dock = "",
        batchSize = 0,
        depot = "",
        status = BackhaulMilestoneStatus.OPEN,
    )
}

package com.acme.transport.linehaul.tender.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered tender milestone is in its life cycle. */
enum class TieredTenderMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered tender milestone as the tender team keeps it. */
data class TieredTenderMilestone(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var graceDays: Int,
    var active: Boolean,
    var description: String,
    var status: TieredTenderMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTenderMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTenderMilestoneStatus): Boolean {
        when (status) {
            TieredTenderMilestoneStatus.OPEN -> return next == TieredTenderMilestoneStatus.ARCHIVED
            TieredTenderMilestoneStatus.IN_REVIEW -> {
                when (next) {
                    TieredTenderMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredTenderMilestoneStatus.RESOLVED -> {
                when (next) {
                    TieredTenderMilestoneStatus.REOPENED -> return true
                }
                return false
            }
            TieredTenderMilestoneStatus.REOPENED -> {
                when (next) {
                    TieredTenderMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredTenderMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("tiered tender milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedLoad(): Double = round((graceDays.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): TieredTenderMilestone {
        return TieredTenderMilestone(
            code = code,
            label = label,
            updatedAt = updatedAt,
            graceDays = graceDays,
            active = active,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered tender milestone in status open. */
fun newTieredTenderMilestone(code: String, label: String): TieredTenderMilestone {
    return TieredTenderMilestone(
        code = code,
        label = label,
        updatedAt = 0,
        graceDays = 0,
        active = false,
        description = "",
        status = TieredTenderMilestoneStatus.OPEN,
    )
}
